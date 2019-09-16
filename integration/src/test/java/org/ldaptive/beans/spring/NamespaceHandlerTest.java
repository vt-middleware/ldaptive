/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import java.time.Period;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.TestControl;
import org.ldaptive.auth.AggregateAuthenticationHandler;
import org.ldaptive.auth.AggregateAuthenticationResponseHandler;
import org.ldaptive.auth.AggregateDnResolver;
import org.ldaptive.auth.AggregateEntryResolver;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.auth.SimpleBindAuthenticationHandler;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.sasl.DigestMD5Config;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
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
  @BeforeClass(groups = "beans-spring")
  public void loadContext()
    throws Exception
  {
    context = new ClassPathXmlApplicationContext(new String[] {"/spring-ext-context.xml", });
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "beans-spring")
  public void closePools()
    throws Exception
  {
    closeConnectionPools(context.getBean("anonymous-search-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("bind-search-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("direct-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("ad-authenticator", Authenticator.class));
    closeConnectionPools(context.getBean("sasl-auth", Authenticator.class));
    closeConnectionPools(context.getBean("aggregate-authenticator", Authenticator.class));

    context.getBean("pooled-connection-factory", PooledConnectionFactory.class).close();
  }


  /**
   * Closing any authentication handler and DN resolver connection pools.
   *
   * @param  auth  to inspect for connection pools
   */
  private void closeConnectionPools(final Authenticator auth)
  {
    try {
      final AuthenticationHandler authHandler = auth.getAuthenticationHandler();
      if (authHandler instanceof ConnectionFactoryManager) {
        ((ConnectionFactoryManager) authHandler).getConnectionFactory().close();
      } else if (authHandler instanceof AggregateAuthenticationHandler) {
        ((AggregateAuthenticationHandler) authHandler).getAuthenticationHandlers().values().stream().filter(
          handler -> handler instanceof ConnectionFactoryManager).forEach(
            handler -> ((ConnectionFactoryManager) handler).getConnectionFactory().close());
      }
      final DnResolver dnResolver = auth.getDnResolver();
      if (dnResolver instanceof ConnectionFactoryManager) {
        ((ConnectionFactoryManager) dnResolver).getConnectionFactory().close();
      } else if (dnResolver instanceof AggregateDnResolver) {
        ((AggregateDnResolver) dnResolver).getDnResolvers().values().stream().filter(
          resolver -> resolver instanceof ConnectionFactoryManager).forEach(
            resolver -> ((ConnectionFactoryManager) resolver).getConnectionFactory().close());
      }
      final EntryResolver entryResolver = auth.getEntryResolver();
      if (entryResolver instanceof ConnectionFactoryManager) {
        if (((ConnectionFactoryManager) entryResolver).getConnectionFactory() != null) {
          ((ConnectionFactoryManager) entryResolver).getConnectionFactory().close();
        }
      } else if (entryResolver instanceof AggregateEntryResolver) {
        ((AggregateEntryResolver) entryResolver).getEntryResolvers().values().stream().filter(
          resolver -> resolver instanceof ConnectionFactoryManager).forEach(
            resolver -> {
              if (((ConnectionFactoryManager) resolver).getConnectionFactory() != null) {
                ((ConnectionFactoryManager) resolver).getConnectionFactory().close();
              }
            });
      }
    } catch (RuntimeException e) {
      throw new RuntimeException("Error closing pools for " + auth, e);
    }
  }


  /**
   * Attempts to load a Spring application context XML files to verify proper wiring.
   */
  @Test(groups = "beans-spring")
  public void testSpringWiring()
  {
    AssertJUnit.assertEquals(7, context.getBeansOfType(Authenticator.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(PooledConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(DefaultConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(SearchOperation.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(ConnectionConfig.class).size());
  }


  /**
   * Test anonymous search authenticator.
   */
  @Test(groups = "beans-spring")
  public void testAnonSearchAuthenticator()
  {
    final Authenticator anonSearchAuthenticator = context.getBean(
      "anonymous-search-authenticator",
      Authenticator.class);
    AssertJUnit.assertNotNull(anonSearchAuthenticator);
    testBindConnectionPool(anonSearchAuthenticator);
    testSearchDnResolver(anonSearchAuthenticator, AuthenticatorType.ANON_SEARCH);
    AssertJUnit.assertNotNull(anonSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNull(anonSearchAuthenticator.getResponseHandlers());
    AssertJUnit.assertNull(
      ((SimpleBindAuthenticationHandler)
        anonSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test bind search authenticator.
   */
  @Test(groups = "beans-spring")
  public void testBindSearchAuthenticator()
  {
    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordPolicyAuthenticationResponseHandler.class,
      bindSearchAuthenticator.getResponseHandlers()[0].getClass());
    AssertJUnit.assertEquals(
      PasswordPolicyControl.class,
      ((SimpleBindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass());
  }


  /**
   * Test bind search authenticator.
   */
  @Test(groups = "beans-spring")
  public void testBindSearchAuthenticatorNoPooling()
  {
    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-disable-pool", Authenticator.class);
    AssertJUnit.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(bindSearchAuthenticator.getResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordPolicyAuthenticationResponseHandler.class,
      bindSearchAuthenticator.getResponseHandlers()[0].getClass());
    AssertJUnit.assertEquals(
      PasswordPolicyControl.class,
      ((SimpleBindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass());
  }


  /**
   * Test sasl bind search authenticator.
   */
  @Test(groups = "beans-spring")
  public void testSaslBindSearchAuthenticator()
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }
    final Authenticator saslBindSearchAuthenticator = context.getBean("sasl-auth", Authenticator.class);
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator);
    testBindConnectionPool(saslBindSearchAuthenticator);
    testSearchDnResolver(saslBindSearchAuthenticator, AuthenticatorType.SASL_SEARCH);
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator.getResponseHandlers());
    AssertJUnit.assertEquals(
      PasswordExpirationAuthenticationResponseHandler.class,
      saslBindSearchAuthenticator.getResponseHandlers()[0].getClass());
    AssertJUnit.assertNull(
      ((SimpleBindAuthenticationHandler)
        saslBindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test direct authenticator.
   */
  @Test(groups = "beans-spring")
  public void testDirectAuthenticator()
  {
    final Authenticator directAuthenticator = context.getBean("direct-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(directAuthenticator);
    testBindConnectionPool(directAuthenticator);
    AssertJUnit.assertNotNull(((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat());
    AssertJUnit.assertTrue(
      ((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat().startsWith("cn=%1$s"));
    AssertJUnit.assertNotNull(directAuthenticator.getResponseHandlers());
    final FreeIPAAuthenticationResponseHandler handler =
      (FreeIPAAuthenticationResponseHandler) directAuthenticator.getResponseHandlers()[0];
    AssertJUnit.assertNotNull(handler);
    AssertJUnit.assertEquals(Period.ofDays(90), handler.getExpirationPeriod());
    AssertJUnit.assertEquals(Period.ofDays(15), handler.getWarningPeriod());
    AssertJUnit.assertEquals(4, handler.getMaxLoginFailures());
    AssertJUnit.assertNull(
      ((SimpleBindAuthenticationHandler)
        directAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test AD authenticator.
   */
  @Test(groups = "beans-spring")
  public void testADAuthenticator()
  {
    final Authenticator adAuthenticator = context.getBean("ad-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(adAuthenticator);
    testBindConnectionPool(adAuthenticator);
    testSearchDnResolver(adAuthenticator, AuthenticatorType.AD);
    AssertJUnit.assertNotNull(adAuthenticator.getEntryResolver());
    AssertJUnit.assertNotNull(adAuthenticator.getResponseHandlers());
    AssertJUnit.assertNotNull(adAuthenticator.getReturnAttributes());
    final ActiveDirectoryAuthenticationResponseHandler handler =
      (ActiveDirectoryAuthenticationResponseHandler) adAuthenticator.getResponseHandlers()[0];
    AssertJUnit.assertNotNull(handler);
    AssertJUnit.assertEquals(Period.ofDays(90), handler.getExpirationPeriod());
    AssertJUnit.assertEquals(Period.ofDays(15), handler.getWarningPeriod());
    AssertJUnit.assertNull(
      ((SimpleBindAuthenticationHandler)
        adAuthenticator.getAuthenticationHandler()).getAuthenticationControls());
  }


  /**
   * Test aggregate authenticator.
   */
  @Test(groups = "beans-spring")
  public void testAggregateAuthenticator()
  {
    final Authenticator aggregateAuthenticator = context.getBean(
      "aggregate-authenticator",
      Authenticator.class);
    AssertJUnit.assertNotNull(aggregateAuthenticator);
    AssertJUnit.assertTrue(aggregateAuthenticator.getDnResolver() instanceof AggregateDnResolver);
    final AggregateDnResolver dnResolvers = (AggregateDnResolver) aggregateAuthenticator.getDnResolver();
    for (DnResolver dnResolver : dnResolvers.getDnResolvers().values()) {
      testSearchDnResolver((SearchDnResolver) dnResolver, null);
    }

    AssertJUnit.assertNotNull(aggregateAuthenticator.getEntryResolver());

    AssertJUnit.assertTrue(
      aggregateAuthenticator.getAuthenticationHandler() instanceof AggregateAuthenticationHandler);
    final AggregateAuthenticationHandler authHandlers =
      (AggregateAuthenticationHandler) aggregateAuthenticator.getAuthenticationHandler();
    for (AuthenticationHandler authHandler : authHandlers.getAuthenticationHandlers().values()) {
      testBindConnectionPool((SimpleBindAuthenticationHandler) authHandler);
    }

    if (aggregateAuthenticator.getResponseHandlers() != null) {
      final AggregateAuthenticationResponseHandler responseHandlers =
        (AggregateAuthenticationResponseHandler) aggregateAuthenticator.getResponseHandlers()[0];
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
   */
  @Test(groups = "beans-spring")
  public void testPooledConnectionFactory()
  {
    final PooledConnectionFactory pooledConnectionFactory = context.getBean(
      "pooled-connection-factory",
      PooledConnectionFactory.class);
    AssertJUnit.assertNotNull(pooledConnectionFactory);
    testPooledConnectionFactory(pooledConnectionFactory, null);
  }


  /**
   * Test connection factory.
   */
  @Test(groups = "beans-spring")
  public void testConnectionFactory()
  {
    final DefaultConnectionFactory connectionFactory = context.getBean(
      "connection-factory",
      DefaultConnectionFactory.class);
    AssertJUnit.assertNotNull(connectionFactory);
    testConnectionConfig(connectionFactory.getConnectionConfig(), null);
  }


  /**
   * Test search operation.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperation()
  {
    final SearchOperation operation = context.getBean("search-operation", SearchOperation.class);
    AssertJUnit.assertNotNull(operation);
    testSearchRequest(operation.getRequest());
  }


  /**
   * Test search operation worker.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperationWorker()
  {
    final SearchOperationWorker operation = context.getBean("search-operation-worker", SearchOperationWorker.class);
    AssertJUnit.assertNotNull(operation);
    testSearchRequest(operation.getOperation().getRequest());
  }


  /**
   * Test connection config.
   */
  @Test(groups = "beans-spring")
  public void testConnectionConfig()
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
    testBindConnectionPool((SimpleBindAuthenticationHandler) auth.getAuthenticationHandler());
  }


  /**
   * Runs asserts against the bind connection pool.
   *
   * @param  authHandler  authenticator handler
   */
  private void testBindConnectionPool(final SimpleBindAuthenticationHandler authHandler)
  {
    if (authHandler.getConnectionFactory() instanceof PooledConnectionFactory) {
      testPooledConnectionFactory((PooledConnectionFactory) authHandler.getConnectionFactory(), null);
    } else {
      testConnectionConfig(((DefaultConnectionFactory) authHandler.getConnectionFactory()).getConnectionConfig(), null);
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
    testSearchDnResolver((SearchDnResolver) auth.getDnResolver(), authType);
  }


  /**
   * Runs asserts against the DN resolver.
   *
   * @param  dnResolver  dn resolver
   * @param  authType  authenticator type
   */
  private void testSearchDnResolver(final SearchDnResolver dnResolver, final AuthenticatorType authType)
  {
    AssertJUnit.assertNotNull(dnResolver.getBaseDn());
    AssertJUnit.assertEquals("(mail={user})", dnResolver.getUserFilter());
    if (dnResolver.getConnectionFactory() instanceof PooledConnectionFactory) {
      testPooledConnectionFactory((PooledConnectionFactory) dnResolver.getConnectionFactory(), authType);
    } else {
      testConnectionConfig(
        ((DefaultConnectionFactory) dnResolver.getConnectionFactory()).getConnectionConfig(), authType);
    }
  }


  /**
   * Runs asserts against a pooled connection factory.
   *
   * @param  factory  to test
   * @param  authType  authenticator type or null
   */
  private void testPooledConnectionFactory(final PooledConnectionFactory factory, final AuthenticatorType authType)
  {
    AssertJUnit.assertEquals(Duration.ofSeconds(3), factory.getBlockWaitTime());
    AssertJUnit.assertFalse(factory.getFailFastInitialize());
    AssertJUnit.assertEquals(Duration.ofMinutes(5), factory.getPruneStrategy().getPrunePeriod());
    AssertJUnit.assertEquals(Duration.ofMinutes(10), ((IdlePruneStrategy) factory.getPruneStrategy()).getIdleTime());
    AssertJUnit.assertEquals(SearchValidator.class, factory.getValidator().getClass());

    final PoolConfig poolConfig = factory.getPoolConfig();
    AssertJUnit.assertEquals(3, poolConfig.getMinPoolSize());
    AssertJUnit.assertEquals(10, poolConfig.getMaxPoolSize());
    AssertJUnit.assertEquals(Duration.ofMinutes(5), poolConfig.getValidatePeriod());
    AssertJUnit.assertFalse(poolConfig.isValidateOnCheckOut());
    AssertJUnit.assertTrue(poolConfig.isValidatePeriodically());

    testConnectionConfig(factory.getConnectionConfig(), authType);
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
    AssertJUnit.assertEquals(Duration.ofSeconds(3), connectionConfig.getConnectTimeout());
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      AssertJUnit.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      AssertJUnit.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }

    if (authType != null) {
      final BindConnectionInitializer ci = connectionConfig.getConnectionInitializers() != null ?
        (BindConnectionInitializer) connectionConfig.getConnectionInitializers()[0] : null;
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
        final DigestMD5Config sc = (DigestMD5Config) ci.getBindSaslConfig();
        AssertJUnit.assertNotNull(sc);
        AssertJUnit.assertEquals(Mechanism.DIGEST_MD5, sc.getMechanism());
        AssertJUnit.assertEquals(QualityOfProtection.AUTH_INT, sc.getQualityOfProtection()[0]);
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
    AssertJUnit.assertNotNull(request.getFilter());
    AssertJUnit.assertTrue(request.getReturnAttributes().length > 0);
    AssertJUnit.assertEquals(SearchScope.ONELEVEL, request.getSearchScope());
    AssertJUnit.assertEquals(Duration.ofSeconds(5), request.getTimeLimit());
    AssertJUnit.assertEquals(10, request.getSizeLimit());
    AssertJUnit.assertTrue(request.getBinaryAttributes().length > 0);
  }
}
