/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import java.time.Period;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.SortBehavior;
import org.ldaptive.TestControl;
import org.ldaptive.auth.AbstractBindAuthenticationHandler;
import org.ldaptive.auth.AbstractSearchDnResolver;
import org.ldaptive.auth.AggregateDnResolver;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.BindAuthenticationHandler;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.concurrent.AggregatePooledSearchExecutor;
import org.ldaptive.concurrent.AggregateSearchExecutor;
import org.ldaptive.concurrent.ParallelPooledSearchExecutor;
import org.ldaptive.concurrent.ParallelSearchExecutor;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for {@link NamespaceHandler}.
 *
 * @author  Middleware Services
 */
public class NamespaceHandlerTest
{

  /** Enum to aid in testing authenticator types. */
  public enum AuthenticatorType {

    /** anonymous search. */
    ANON_SEARCH,

    /** bind search. */
    BIND_SEARCH,

    /** sasl search. */
    SASL_SEARCH,

    /** direct. */
    DIRECT,

    /** active directory. */
    AD
  }

  /** Spring context to test. */
  private ClassPathXmlApplicationContext context;


  /** @throws  Exception  On test failure. */
  @BeforeClass(groups = {"beans-spring"})
  public void loadContext()
    throws Exception
  {
    context = new ClassPathXmlApplicationContext(new String[] {"/spring-ext-context.xml", });
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"beans-spring"})
  public void closePools()
    throws Exception
  {
    closeConnectionPools(context.getBean("anonymous-search-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("bind-search-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("direct-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("ad-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("sasl-auth", Authenticator.class));
    closeConnectionPools(context.getBean("aggregate-authenticator", Authenticator.class));

    context.getBean("pooled-connection-factory", PooledConnectionFactory.class).getConnectionPool().close();
    context.getBean("connection-pool", BlockingConnectionPool.class).close();
  }


  /**
   * Closing any authentication handler and DN resolver connection pools.
   *
   * @param  auth  to inspect for connection pools
   */
  private void closeConnectionPools(final Authenticator auth)
  {
    final AuthenticationHandler authHandler = auth.getAuthenticationHandler();
    if (authHandler instanceof PooledConnectionFactoryManager) {
      ((PooledConnectionFactoryManager) authHandler).getConnectionFactory().getConnectionPool().close();
    } else if (authHandler instanceof AggregateDnResolver.AuthenticationHandler) {
      ((AggregateDnResolver.AuthenticationHandler) authHandler).getAuthenticationHandlers().values().stream().filter(
        handler -> handler instanceof PooledConnectionFactoryManager).forEach(
          handler -> ((PooledConnectionFactoryManager) handler).getConnectionFactory().getConnectionPool().close());
    }
    final DnResolver dnResolver = auth.getDnResolver();
    if (dnResolver instanceof PooledConnectionFactoryManager) {
      ((PooledConnectionFactoryManager) dnResolver).getConnectionFactory().getConnectionPool().close();
    } else if (dnResolver instanceof AggregateDnResolver) {
      ((AggregateDnResolver) dnResolver).getDnResolvers().values().stream().filter(
        resolver -> resolver instanceof PooledConnectionFactoryManager).forEach(
          resolver -> ((PooledConnectionFactoryManager) resolver).getConnectionFactory().getConnectionPool().close());
    }
    final EntryResolver entryResolver = auth.getEntryResolver();
    if (entryResolver instanceof PooledConnectionFactoryManager) {
      ((PooledConnectionFactoryManager) entryResolver).getConnectionFactory().getConnectionPool().close();
    } else if (entryResolver instanceof AggregateDnResolver.EntryResolver) {
      ((AggregateDnResolver.EntryResolver) entryResolver).getEntryResolvers().values().stream().filter(
        resolver -> resolver instanceof PooledConnectionFactoryManager).forEach(
          resolver -> ((PooledConnectionFactoryManager) resolver).getConnectionFactory().getConnectionPool().close());
    }
  }


  /**
   * Attempts to load a Spring application context XML files to verify proper wiring.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testSpringWiring()
    throws Exception
  {
    AssertJUnit.assertEquals(7, context.getBeansOfType(Authenticator.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(PooledConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(DefaultConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(SearchExecutor.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(BlockingConnectionPool.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(ConnectionConfig.class).size());
  }


  /**
   * Test anonymous search authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testAnonSearchAuthenticator()
    throws Exception
  {
    final Authenticator anonSearchAuthenticator = context.getBean(
      "anonymous-search-authenticator",
      Authenticator.class);
    AssertJUnit.assertNotNull(anonSearchAuthenticator);
    testBindConnectionPool(anonSearchAuthenticator);
    testSearchDnResolver(anonSearchAuthenticator, AuthenticatorType.ANON_SEARCH);
    AssertJUnit.assertNotNull(anonSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNull(anonSearchAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertNull(
      ((PooledBindAuthenticationHandler)
        anonSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test bind search authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testBindSearchAuthenticator()
    throws Exception
  {
    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordPolicyAuthenticationResponseHandler.class,
      bindSearchAuthenticator.getAuthenticationResponseHandlers()[0].getClass());
    AssertJUnit.assertEquals(
      PasswordPolicyControl.class,
      ((PooledBindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass());
  }


  /**
   * Test bind search authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testBindSearchAuthenticatorNoPooling()
    throws Exception
  {
    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-disable-pool", Authenticator.class);
    AssertJUnit.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordPolicyAuthenticationResponseHandler.class,
      bindSearchAuthenticator.getAuthenticationResponseHandlers()[0].getClass());
    AssertJUnit.assertEquals(
      PasswordPolicyControl.class,
      ((BindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass());
  }


  /**
   * Test sasl bind search authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testSaslBindSearchAuthenticator()
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }
    final Authenticator saslBindSearchAuthenticator = context.getBean("sasl-auth", Authenticator.class);
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator);
    testBindConnectionPool(saslBindSearchAuthenticator);
    testSearchDnResolver(saslBindSearchAuthenticator, AuthenticatorType.SASL_SEARCH);
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordExpirationAuthenticationResponseHandler.class,
      saslBindSearchAuthenticator.getAuthenticationResponseHandlers()[0].getClass());
    AssertJUnit.assertNull(
      ((PooledBindAuthenticationHandler)
        saslBindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test direct authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testDirectAuthenticator()
    throws Exception
  {
    final Authenticator directAuthenticator = context.getBean("direct-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(directAuthenticator);
    testBindConnectionPool(directAuthenticator);
    AssertJUnit.assertNotNull(((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat());
    AssertJUnit.assertTrue(
      ((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat().startsWith("cn=%1$s"));
    AssertJUnit.assertNotNull(directAuthenticator.getAuthenticationResponseHandlers());
    final FreeIPAAuthenticationResponseHandler handler =
      (FreeIPAAuthenticationResponseHandler) directAuthenticator.getAuthenticationResponseHandlers()[0];
    AssertJUnit.assertNotNull(handler);
    AssertJUnit.assertEquals(Period.ofDays(90), handler.getExpirationPeriod());
    AssertJUnit.assertEquals(Period.ofDays(15), handler.getWarningPeriod());
    AssertJUnit.assertEquals(4, handler.getMaxLoginFailures());
    AssertJUnit.assertNull(
      ((PooledBindAuthenticationHandler)
        directAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test AD authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testADAuthenticator()
    throws Exception
  {
    final Authenticator adAuthenticator = context.getBean("ad-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(adAuthenticator);
    testBindConnectionPool(adAuthenticator);
    testSearchDnResolver(adAuthenticator, AuthenticatorType.AD);
    AssertJUnit.assertNotNull(adAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(adAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertNotNull(adAuthenticator.getReturnAttributes());
    final ActiveDirectoryAuthenticationResponseHandler handler =
      (ActiveDirectoryAuthenticationResponseHandler) adAuthenticator.getAuthenticationResponseHandlers()[0];
    AssertJUnit.assertNotNull(handler);
    AssertJUnit.assertEquals(Period.ofDays(90), handler.getExpirationPeriod());
    AssertJUnit.assertEquals(Period.ofDays(15), handler.getWarningPeriod());
    AssertJUnit.assertNull(
      ((PooledBindAuthenticationHandler)
        adAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test aggregate authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testAggregateAuthenticator()
    throws Exception
  {
    final Authenticator aggregateAuthenticator = context.getBean(
      "aggregate-authenticator",
      Authenticator.class);
    AssertJUnit.assertNotNull(aggregateAuthenticator);
    AssertJUnit.assertTrue(aggregateAuthenticator.getDnResolver() instanceof AggregateDnResolver);
    final AggregateDnResolver dnResolvers = (AggregateDnResolver) aggregateAuthenticator.getDnResolver();
    for (DnResolver dnResolver : dnResolvers.getDnResolvers().values()) {
      testSearchDnResolver((AbstractSearchDnResolver) dnResolver, null);
    }

    AssertJUnit.assertNotNull(aggregateAuthenticator.getEntryResolver());

    AssertJUnit.assertTrue(
      aggregateAuthenticator.getAuthenticationHandler() instanceof AggregateDnResolver.AuthenticationHandler);
    final AggregateDnResolver.AuthenticationHandler authHandlers =
      (AggregateDnResolver.AuthenticationHandler) aggregateAuthenticator.getAuthenticationHandler();
    for (AuthenticationHandler authHandler : authHandlers.getAuthenticationHandlers().values()) {
      testBindConnectionPool((AbstractBindAuthenticationHandler) authHandler);
    }

    if (aggregateAuthenticator.getAuthenticationResponseHandlers() != null) {
      final AggregateDnResolver.AuthenticationResponseHandler responseHandlers =
        (AggregateDnResolver.AuthenticationResponseHandler)
          aggregateAuthenticator.getAuthenticationResponseHandlers()[0];
      for (AuthenticationResponseHandler[] responseHandler :
           responseHandlers.getAuthenticationResponseHandlers().values()) {
        for (AuthenticationResponseHandler handler : responseHandler) {
          AssertJUnit.assertNotNull(handler);
        }
      }
    }
  }


  /**
   * Test pooled connection factory.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testPooledConnectionFactory()
    throws Exception
  {
    final PooledConnectionFactory pooledConnectionFactory = context.getBean(
      "pooled-connection-factory",
      PooledConnectionFactory.class);
    AssertJUnit.assertNotNull(pooledConnectionFactory);
    testConnectionPool((BlockingConnectionPool) pooledConnectionFactory.getConnectionPool(), null);
  }


  /**
   * Test connection factory.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testConnectionFactory()
    throws Exception
  {
    final DefaultConnectionFactory connectionFactory = context.getBean(
      "connection-factory",
      DefaultConnectionFactory.class);
    AssertJUnit.assertNotNull(connectionFactory);
    testConnectionConfig(connectionFactory.getConnectionConfig(), null);
  }


  /**
   * Test search executor.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testSearchExecutor()
    throws Exception
  {
    final SearchExecutor executor = context.getBean("search-executor", SearchExecutor.class);
    AssertJUnit.assertNotNull(executor);
    testSearchRequest(executor);
  }


  /**
   * Test parallel search executor.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testParallelSearchExecutor()
    throws Exception
  {
    final ParallelSearchExecutor executor = context.getBean("parallel-search-executor", ParallelSearchExecutor.class);
    AssertJUnit.assertNotNull(executor);
    testSearchRequest(executor);
  }


  /**
   * Test parallel pooled search executor.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testParallelPooledSearchExecutor()
    throws Exception
  {
    final ParallelPooledSearchExecutor executor = context.getBean(
      "parallel-pooled-search-executor",
      ParallelPooledSearchExecutor.class);
    AssertJUnit.assertNotNull(executor);
    testSearchRequest(executor);
  }


  /**
   * Test aggregate search executor.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testAggregateSearchExecutor()
    throws Exception
  {
    final AggregateSearchExecutor executor = context.getBean(
      "aggregate-search-executor",
      AggregateSearchExecutor.class);
    AssertJUnit.assertNotNull(executor);
    testSearchRequest(executor);
  }


  /**
   * Test aggregate pooled search executor.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testAggregatePooledSearchExecutor()
    throws Exception
  {
    final AggregatePooledSearchExecutor executor = context.getBean(
      "aggregate-pooled-search-executor",
      AggregatePooledSearchExecutor.class);
    AssertJUnit.assertNotNull(executor);
    testSearchRequest(executor);
  }


  /**
   * Test connection pool.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testConnectionPool()
    throws Exception
  {
    final BlockingConnectionPool connectionPool = context.getBean("connection-pool", BlockingConnectionPool.class);
    AssertJUnit.assertNotNull(connectionPool);
    testConnectionPool(connectionPool, null);
  }


  /**
   * Test connection config.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testConnectionConfig()
    throws Exception
  {
    final ConnectionConfig config = context.getBean("connection-config", ConnectionConfig.class);
    AssertJUnit.assertNotNull(config);
    testConnectionConfig(config, null);
  }


  /**
   * Runs asserts against the bind connection pool.
   *
   * @param  auth  authenticator
   */
  private void testBindConnectionPool(final Authenticator auth)
  {
    testBindConnectionPool((AbstractBindAuthenticationHandler) auth.getAuthenticationHandler());
  }


  /**
   * Runs asserts against the bind connection pool.
   *
   * @param  authHandler  authenticator handler
   */
  private void testBindConnectionPool(final AbstractBindAuthenticationHandler authHandler)
  {
    if (authHandler instanceof PooledBindAuthenticationHandler) {
      final BlockingConnectionPool pool =
        (BlockingConnectionPool)
          ((PooledConnectionFactoryManager) authHandler).getConnectionFactory().getConnectionPool();
      testConnectionPool(pool, null);
    } else {
      Connection conn = null;
      try {
        conn = ((ConnectionFactoryManager) authHandler).getConnectionFactory().getConnection();
        testConnectionConfig(conn.getConnectionConfig(), null);
      } catch (LdapException e) {
        AssertJUnit.fail("Error getting connection: " + e.getMessage());
      } finally {
        if (conn != null) {
          conn.close();
        }
      }
    }
  }


  /**
   * Runs asserts against the DN resolver.
   *
   * @param  auth  authenticator
   * @param  authType  authenticator type
   */
  private void testSearchDnResolver(final Authenticator auth, final AuthenticatorType authType)
  {
    testSearchDnResolver((AbstractSearchDnResolver) auth.getDnResolver(), authType);
  }


  /**
   * Runs asserts against the DN resolver.
   *
   * @param  dnResolver  dn resolver
   * @param  authType  authenticator type
   */
  private void testSearchDnResolver(final AbstractSearchDnResolver dnResolver, final AuthenticatorType authType)
  {
    AssertJUnit.assertNotNull(dnResolver.getBaseDn());
    AssertJUnit.assertEquals("(mail={user})", dnResolver.getUserFilter());
    if (dnResolver instanceof PooledSearchDnResolver) {
      final BlockingConnectionPool pool =
        (BlockingConnectionPool)
          ((PooledConnectionFactoryManager) dnResolver).getConnectionFactory().getConnectionPool();
      testConnectionPool(pool, authType);
    } else {
      Connection conn = null;
      try {
        conn = ((ConnectionFactoryManager) dnResolver).getConnectionFactory().getConnection();
        testConnectionConfig(conn.getConnectionConfig(), authType);
      } catch (LdapException e) {
        AssertJUnit.fail("Error getting connection: " + e.getMessage());
      } finally {
        if (conn != null) {
          conn.close();
        }
      }
    }
  }


  /**
   * Runs asserts against the connection pool.
   *
   * @param  pool  to test
   * @param  authType  authenticator type or null
   */
  private void testConnectionPool(final BlockingConnectionPool pool, final AuthenticatorType authType)
  {
    AssertJUnit.assertEquals(Duration.ofSeconds(3), pool.getBlockWaitTime());
    AssertJUnit.assertFalse(pool.getFailFastInitialize());
    AssertJUnit.assertEquals(Duration.ofMinutes(5), pool.getPruneStrategy().getPrunePeriod());
    AssertJUnit.assertEquals(Duration.ofMinutes(10), ((IdlePruneStrategy) pool.getPruneStrategy()).getIdleTime());
    AssertJUnit.assertEquals(SearchValidator.class, pool.getValidator().getClass());

    final PoolConfig poolConfig = pool.getPoolConfig();
    AssertJUnit.assertEquals(3, poolConfig.getMinPoolSize());
    AssertJUnit.assertEquals(10, poolConfig.getMaxPoolSize());
    AssertJUnit.assertEquals(Duration.ofMinutes(5), poolConfig.getValidatePeriod());
    AssertJUnit.assertFalse(poolConfig.isValidateOnCheckOut());
    AssertJUnit.assertTrue(poolConfig.isValidatePeriodically());

    Connection conn = null;
    try {
      conn = pool.getConnection();
      testConnectionConfig(conn.getConnectionConfig(), authType);
    } catch (LdapException e) {
      AssertJUnit.fail("Error getting connection from pool: " + e.getMessage());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }


  /**
   * Runs asserts against the connection config.
   *
   * @param  connectionConfig  to test
   * @param  authType  authenticator type or null
   */
  private void testConnectionConfig(final ConnectionConfig connectionConfig, final AuthenticatorType authType)
  {
    AssertJUnit.assertNotNull(connectionConfig.getLdapUrl());
    AssertJUnit.assertTrue(connectionConfig.getUseStartTLS());
    AssertJUnit.assertFalse(connectionConfig.getUseSSL());
    AssertJUnit.assertEquals(Duration.ofSeconds(3), connectionConfig.getConnectTimeout());
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      AssertJUnit.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      AssertJUnit.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }

    if (authType != null) {
      final BindConnectionInitializer ci = (BindConnectionInitializer) connectionConfig.getConnectionInitializer();
      switch(authType) {
      case ANON_SEARCH:
        AssertJUnit.assertNull(ci);
        break;
      case BIND_SEARCH:
        AssertJUnit.assertNotNull(ci);
        AssertJUnit.assertNotNull(ci.getBindDn());
        AssertJUnit.assertNotNull(ci.getBindCredential());
        break;
      case SASL_SEARCH:
        AssertJUnit.assertNotNull(ci);
        final SaslConfig sc = ci.getBindSaslConfig();
        AssertJUnit.assertNotNull(sc);
        AssertJUnit.assertEquals(Mechanism.DIGEST_MD5, sc.getMechanism());
        AssertJUnit.assertEquals(QualityOfProtection.AUTH_INT, sc.getQualityOfProtection());
        AssertJUnit.assertEquals(SecurityStrength.MEDIUM, sc.getSecurityStrength());
        break;
      case DIRECT:
        AssertJUnit.assertNull(ci);
        break;
      case AD:
        AssertJUnit.assertNotNull(ci);
        AssertJUnit.assertNotNull(ci.getBindDn());
        AssertJUnit.assertNotNull(ci.getBindCredential());
        break;
      default:
        throw new IllegalStateException("Unknown type");
      }
    }
  }


  /**
   * Runs asserts against the search request.
   *
   * @param  request  to test
   */
  private void testSearchRequest(final SearchRequest request)
  {
    AssertJUnit.assertNotNull(request.getBaseDn());
    AssertJUnit.assertTrue(request.getBaseDn().length() > 0);
    AssertJUnit.assertNotNull(request.getSearchFilter());
    AssertJUnit.assertTrue(request.getReturnAttributes().length > 0);
    AssertJUnit.assertEquals(SearchScope.ONELEVEL, request.getSearchScope());
    AssertJUnit.assertEquals(Duration.ofSeconds(5), request.getTimeLimit());
    AssertJUnit.assertEquals(10, request.getSizeLimit());
    AssertJUnit.assertTrue(request.getBinaryAttributes().length > 0);
    AssertJUnit.assertEquals(SortBehavior.ORDERED, request.getSortBehavior());
  }
}
