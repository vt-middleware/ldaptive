/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.setup_teardown;

import java.io.IOException;
import java.util.Properties;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.LdapUtils;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.ssl.AllowAnyTrustManager;
import org.ldaptive.ssl.SslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration that is required for all ThreadGroups in a TestPlan. This will be instantiated in a setupThreadGroup
 * that will execute before all other ThreadGroups.
 *
 * @author Middleware Services
 */
public final class TestPlanConfig
{

  /** Logger instance */
  private static final Logger LOGGER = LoggerFactory.getLogger(TestPlanConfig.class);

  /**
   * Config to be used with connections factories that are doing LDAP search operations.
   * This is shared across threads in a test plan.
   */
  private static ConnectionConfig searchConnectionConfig;

  /**
   * Config to be used with connections factories that are doing LDAP authentication operations.
   * This is shared across threads in a test plan.
   */
  private static ConnectionConfig authConnectionConfig;

  /**
   * {@link SingleConnectionFactory} to be used for tests performing LDAP search operations.
   * This is shared across threads in a test plan.
   */
  private static SingleConnectionFactory singleConnectionFactory;

  /**
   * {@link PooledConnectionFactory} to be used for tests performing LDAP search operations.
   * This is shared across threads in a test plan.
   */
  private static PooledConnectionFactory pooledConnectionFactory;

  /**
   * {@link DefaultConnectionFactory} to be used for tests performing LDAP search operations.
   * This is shared across threads in a test plan.
   */
  private static DefaultConnectionFactory defaultConnectionFactory;

  /**
   * {@link DefaultConnectionFactory} to be used for tests performing LDAP authentication operations.
   * This is shared across threads in a test plan.
   */
  private static DefaultConnectionFactory authDefaultConnectionFactory;

  /**
   * Properties for JMeter Sampler tests that are partially loaded from runtime properties defined via docker-compose
   */
  private static JMeterRuntimeProperties properties;

  /** Flag indicating whether the initial configuration for this test plan was successful. */
  private static volatile boolean setupSuccess;

  /** Default private constructor. All access should occur via static functions. */
  private TestPlanConfig() {}


  /**
   * This is the only method that should be used for configuring connection factories for a
   * {@link org.apache.jmeter.testelement.TestPlan}.
   * This should always be called from a {@link org.apache.jmeter.threads.SetupThreadGroup} to help prevent
   * multi-threading issues.
   *
   * @param type of connection(s) to set up for this test plan.
   *
   * @param context the context to run with. This provides access to initialization parameters.
   */
  public static void initConnections(final ConnectionType type, final JavaSamplerContext context)
  {
    try {
      final Properties testRuntimeProps = new Properties();
      testRuntimeProps.load(LdapUtils.getResource("classpath:/org/ldaptive/ldap.properties"));
      properties = new JMeterRuntimeProperties(testRuntimeProps, context);
      switch (type) {
      case ALL:
        initAllConnections();
        break;
      case AUTH_DEFAULT_FACTORY:
        initAuthDefaultConnectionFactory();
        break;
      case SEARCH_ALL:
        initSingleConnectionFactory();
        initPooledConnectionFactory();
        initSearchDefaultConnectionFactory();
        break;
      case SEARCH_DEFAULT_FACTORY:
        initSearchDefaultConnectionFactory();
        break;
      case SEARCH_SINGLE_FACTORY:
        initSingleConnectionFactory();
        break;
      case SEARCH_POOLED_FACTORY:
        initPooledConnectionFactory();
        break;
      default:
        throw new IllegalStateException("Unknown type: " + type);
      }
      setupSuccess = true;
    } catch (LdapException | IOException e) {
      setupSuccess = false;
      LOGGER.error("Setup of Connection(s) failed", e);
    }
  }


  /**
   * Reset all static configuration data. This is useful when running test plans via the GUI as we want connections to
   * be reset between plan executions and this will only happen by reloading the class loader (restarting the GUI).
   */
  public static void teardownConfiguration()
  {
    setupSuccess = false;
    searchConnectionConfig = null;
    authConnectionConfig = null;
    properties = null;
    if (singleConnectionFactory != null) {
      singleConnectionFactory.close();
      singleConnectionFactory = null;
    }
    if (pooledConnectionFactory != null) {
      pooledConnectionFactory.close();
      pooledConnectionFactory = null;
    }
    if (defaultConnectionFactory != null) {
      defaultConnectionFactory.close();
      defaultConnectionFactory = null;
    }
    if (authDefaultConnectionFactory != null) {
      authDefaultConnectionFactory.close();
      authDefaultConnectionFactory = null;
    }
  }


  /**
   * Initialize all of the connections that are used in the Ldaptive load tests. This can be updated to include
   * others if load testing expands beyond what is here.
   *
   * @throws LdapException when there is an error creating/initializing connections
   */
  private static void initAllConnections()
    throws LdapException
  {
    initSearchConnectionConfig();
    initAuthConnectionConfig();
    initSingleConnectionFactory();
    initPooledConnectionFactory();
    initSearchDefaultConnectionFactory();
    initAuthDefaultConnectionFactory();
  }


  /**
   * Initialize a {@link SingleConnectionFactory} for LDAP searches
   *
   * @throws LdapException when there is an error creating/initializing connection
   */
  private static void initSingleConnectionFactory()
    throws LdapException
  {
    initSearchConnectionConfig();
    if (singleConnectionFactory == null) {
      singleConnectionFactory = newSingleConnectionFactory(searchConnectionConfig);
    }
  }


  /**
   * Initialize a {@link PooledConnectionFactory} for LDAP searches
   *
   * @throws LdapException when there is an error creating/initializing connection
   */
  private static void initPooledConnectionFactory()
    throws LdapException
  {
    initSearchConnectionConfig();
    if (pooledConnectionFactory == null) {
      pooledConnectionFactory = newPooledConnectionFactory(searchConnectionConfig);
    }
  }


  /**
   * Initialize a {@link DefaultConnectionFactory} for LDAP searches
   *
   * @throws LdapException when there is an error creating/initializing connection
   */
  private static void initSearchDefaultConnectionFactory()
    throws LdapException
  {
    initSearchConnectionConfig();
    if (defaultConnectionFactory == null) {
      defaultConnectionFactory = newDefaultConnectionFactory(searchConnectionConfig);
    }
  }


  /**
   * Initialize a {@link DefaultConnectionFactory} for LDAP Authentication
   *
   * @throws LdapException when there is an error creating/initializing connection
   */
  private static void initAuthDefaultConnectionFactory()
    throws LdapException
  {
    initAuthConnectionConfig();
    if (authDefaultConnectionFactory == null) {
      authDefaultConnectionFactory = newDefaultConnectionFactory(authConnectionConfig);
    }
  }


  /**
   * Initialize a {@link ConnectionConfig} for connections performing LDAP Search operations
   *
   * @throws LdapException when there is an error creating the config
   */
  private static void initSearchConnectionConfig()
    throws LdapException
  {
    if (searchConnectionConfig == null) {
      searchConnectionConfig = newSearchConnConfig();
    }
  }


  /**
   * Initialize a {@link ConnectionConfig} for connections performing LDAP Authentication operations
   *
   * @throws LdapException when there is an error creating the config
   */
  private static void initAuthConnectionConfig()
    throws LdapException
  {
    if (authConnectionConfig == null) {
      authConnectionConfig = newAuthConnConfig();
    }
  }


  /**
   * Get the shared {@link SingleConnectionFactory} for this {@link org.apache.jmeter.testelement.TestPlan}
   *
   * @return initialized factory
   */
  public static SingleConnectionFactory getSingleConnectionFactory()
  {
    return singleConnectionFactory;
  }


  /**
   * Get the shared {@link PooledConnectionFactory} for this {@link org.apache.jmeter.testelement.TestPlan}
   *
   * @return initialized factory
   */
  public static PooledConnectionFactory getPooledConnectionFactory()
  {
    return pooledConnectionFactory;
  }


  /**
   * Get the shared {@link DefaultConnectionFactory} for this {@link org.apache.jmeter.testelement.TestPlan}
   *
   * This should be used for LDAP Search Operations
   *
   * @return initialized factory
   */
  public static DefaultConnectionFactory getDefaultConnectionFactory()
  {
    return defaultConnectionFactory;
  }


  /**
   * Get the shared {@link DefaultConnectionFactory} for this {@link org.apache.jmeter.testelement.TestPlan}
   *
   * This should be used for LDAP Authentication Operations
   *
   * @return initialized factory
   */
  public static DefaultConnectionFactory getAuthDefaultConnectionFactory()
  {
    return authDefaultConnectionFactory;
  }


  /**
   * Flag indicating whether configuration was successful for this {@link org.apache.jmeter.testelement.TestPlan}
   * This should be checked before tests are executed and before the test
   * {@link org.apache.jmeter.samplers.SampleResult} is started.
   *
   * @return if initial plan config was successful
   */
  public static boolean isSetupSuccess()
  {
    return setupSuccess;
  }


  /**
   * Get shared runtime properties for JMeter tests
   *
   * @return initialized properties
   */
  public static JMeterRuntimeProperties getProperties()
  {
    return properties;
  }


  /**
   * Create and initialize a new (shared) {@link DefaultConnectionFactory} for Search or Authentication operations.
   *
   * NOTE: use {@link #newSearchConnConfig()} when factory is being used for Search Operations
   *       OR
   *       use {@link #newAuthConnConfig()} when factory is being used for Authentication Operations
   *
   * @param config for this connectionFactory
   *
   * @return initialized factory
   *
   * @throws LdapException when a connection cannot be opened/closed as a test of being properly initialized
   */
  private static DefaultConnectionFactory newDefaultConnectionFactory(final ConnectionConfig config)
    throws LdapException
  {
    final DefaultConnectionFactory factory = new DefaultConnectionFactory(config);
    try {
      factory.getConnection().open();
      factory.getConnection().close();
    } catch (LdapException e) {
      throw new LdapException("DefaultConnectionFactory initialization failed", e);
    }
    return factory;
  }


  /**
   * Create and initialize a new (shared) {@link SingleConnectionFactory} for Search operations
   *
   * @param config for this connectionFactory
   *
   * @return initialized factory
   *
   * @throws LdapException when factory cannot be initialized.
   */
  private static SingleConnectionFactory newSingleConnectionFactory(final ConnectionConfig config)
    throws LdapException
  {
    final SingleConnectionFactory factory = new SingleConnectionFactory(config);
    try {
      factory.initialize();
    } catch (LdapException e) {
      throw new LdapException("SingleConnectionFactory initialization failed", e);
    }
    return factory;
  }


  /**
   * Create and initialize a new (shared) {@link PooledConnectionFactory} for Search operations
   *
   * @param config for this connectionFactory
   *
   * @return initialized factory
   *
   * @throws LdapException when factory cannot be initialized.
   */
  private static PooledConnectionFactory newPooledConnectionFactory(final ConnectionConfig config)
    throws LdapException
  {
    try {
      final int minPoolSize = properties.minPoolSize();
      final int maxPoolSize = properties.maxPoolSize();
      final PooledConnectionFactory factory = PooledConnectionFactory.builder()
        .config(config)
        .config(PoolConfig.builder().min(minPoolSize).max(maxPoolSize).build())
        .build();
      factory.initialize();
      return factory;
    } catch (Exception e) {
      throw new LdapException("PooledConnectionFactory initialization failed", e);
    }
  }


  /**
   * {@link org.ldaptive.ConnectionConfig.Builder} with default configuration
   **
   * @return builder with the minimum configuration already performed.
   *
   * @throws LdapException when configuration cannot be created. This is generally due to Test Properties not being
   * configured correctly in the {@link org.apache.jmeter.testelement.TestPlan}
   */
  private static ConnectionConfig.Builder connectionConfigDefaultBuilder()
    throws LdapException
  {
    try {
      final String ldapUrl = properties.ldapUrl();
      final boolean useStartTls = properties.useStartTls();
      final boolean autoReconnect = properties.autoReconnect();

      return ConnectionConfig.builder()
        .url(new LdapURL(ldapUrl).getHostnameWithSchemeAndPort())
        .useStartTLS(useStartTls)
        .sslConfig(SslConfig.builder().trustManagers(new AllowAnyTrustManager()).build())
        .autoReconnect(autoReconnect);
    } catch (Exception ex) {
      throw new LdapException("Setup failed for ConnectionConfig", ex);
    }
  }


  /**
   * Fully configured {@link ConnectionConfig} that can be used for factories that are doing search operations.
   *
   * @return fully configured connection config
   *
   * @throws LdapException if config cannot be created
   */
  private static ConnectionConfig newSearchConnConfig()
    throws LdapException
  {
    return connectionConfigDefaultBuilder().build();
  }


  /**
   * Fully configured {@link ConnectionConfig} that can be used for factories that are doing authentication
   * operations.
   *
   * @return fully configured connection config
   *
   * @throws LdapException if config cannot be created
   */
  private static ConnectionConfig newAuthConnConfig()
    throws LdapException
  {
    final ConnectionConfig.Builder connectionBuilder = connectionConfigDefaultBuilder();
    final BindConnectionInitializer bindConnectionInitializer =
      new BindConnectionInitializer(properties.bindDn(), new Credential(
        properties.bindCredential()));
    connectionBuilder.connectionInitializers(bindConnectionInitializer);
    return connectionBuilder.build();
  }


  /**
   * Enum for specifying the type of connections that should be initialized for this test plan.
   */
  public enum ConnectionType
  {
    /** All connection factories should be configured and initialized */
    ALL,
    /** Only the DefaultConnectionFactory used for Auth operations should be configured and initialized */
    AUTH_DEFAULT_FACTORY,
    /** All connection factories associated with Search operations should be configured and initialized */
    SEARCH_ALL,
    /** Only the DefaultConnectionFactory used for Search operations should be configured and initialized */
    SEARCH_DEFAULT_FACTORY,
    /** Only the SingleConnectionFactory used for Search operations should be configured and initialized */
    SEARCH_SINGLE_FACTORY,
    /** Only the PooledConnectionFactory used for Search operations should be configured and initialized */
    SEARCH_POOLED_FACTORY
  }
}
