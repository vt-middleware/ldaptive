/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import java.time.Period;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.RoundRobinConnectionStrategy;
import org.ldaptive.SearchConnectionValidator;
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
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.QualityOfProtection;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.sasl.SecurityStrength;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
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
    Assert.assertEquals(context.getBeansOfType(Authenticator.class).size(), 7);
    Assert.assertEquals(context.getBeansOfType(PooledConnectionFactory.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(DefaultConnectionFactory.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(SearchOperation.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(ConnectionConfig.class).size(), 1);
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
    Assert.assertNotNull(anonSearchAuthenticator);
    testBindConnectionPool(anonSearchAuthenticator);
    testSearchDnResolver(anonSearchAuthenticator, AuthenticatorType.ANON_SEARCH);
    Assert.assertNotNull(anonSearchAuthenticator.getEntryResolver());
    Assert.assertNull(anonSearchAuthenticator.getResponseHandlers());
    Assert.assertNull(
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
    Assert.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    Assert.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    Assert.assertNotNull(bindSearchAuthenticator.getResponseHandlers());
    Assert.assertEquals(
      bindSearchAuthenticator.getResponseHandlers()[0].getClass(),
      PasswordPolicyAuthenticationResponseHandler.class);
    Assert.assertEquals(
      ((SimpleBindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass(),
      PasswordPolicyControl.class);
  }


  /**
   * Test bind search authenticator.
   */
  @Test(groups = "beans-spring")
  public void testBindSearchAuthenticatorNoPooling()
  {
    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-disable-pool", Authenticator.class);
    Assert.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator, AuthenticatorType.BIND_SEARCH);
    Assert.assertNotNull(bindSearchAuthenticator.getEntryResolver());
    Assert.assertNotNull(bindSearchAuthenticator.getResponseHandlers());
    Assert.assertEquals(
      bindSearchAuthenticator.getResponseHandlers()[0].getClass(),
      PasswordPolicyAuthenticationResponseHandler.class);
    Assert.assertEquals(
      ((SimpleBindAuthenticationHandler)
        bindSearchAuthenticator.getAuthenticationHandler()).getAuthenticationControls()[0].getClass(),
      PasswordPolicyControl.class);
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
    Assert.assertNotNull(saslBindSearchAuthenticator);
    testBindConnectionPool(saslBindSearchAuthenticator);
    testSearchDnResolver(saslBindSearchAuthenticator, AuthenticatorType.SASL_SEARCH);
    Assert.assertNotNull(saslBindSearchAuthenticator.getEntryResolver());
    Assert.assertNotNull(saslBindSearchAuthenticator.getResponseHandlers());
    Assert.assertEquals(
      saslBindSearchAuthenticator.getResponseHandlers()[0].getClass(),
      PasswordExpirationAuthenticationResponseHandler.class);
    Assert.assertNull(
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
    Assert.assertNotNull(directAuthenticator);
    testBindConnectionPool(directAuthenticator);
    Assert.assertNotNull(((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat());
    Assert.assertTrue(
      ((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat().startsWith("cn=%1$s"));
    Assert.assertNotNull(directAuthenticator.getResponseHandlers());
    final FreeIPAAuthenticationResponseHandler handler =
      (FreeIPAAuthenticationResponseHandler) directAuthenticator.getResponseHandlers()[0];
    Assert.assertNotNull(handler);
    Assert.assertEquals(handler.getExpirationPeriod(), Period.ofDays(90));
    Assert.assertEquals(handler.getWarningPeriod(), Period.ofDays(15));
    Assert.assertEquals(handler.getMaxLoginFailures(), 4);
    Assert.assertNull(
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
    Assert.assertNotNull(adAuthenticator);
    testBindConnectionPool(adAuthenticator);
    testSearchDnResolver(adAuthenticator, AuthenticatorType.AD);
    Assert.assertNotNull(adAuthenticator.getEntryResolver());
    Assert.assertNotNull(adAuthenticator.getResponseHandlers());
    Assert.assertNotNull(adAuthenticator.getReturnAttributes());
    final ActiveDirectoryAuthenticationResponseHandler handler =
      (ActiveDirectoryAuthenticationResponseHandler) adAuthenticator.getResponseHandlers()[0];
    Assert.assertNotNull(handler);
    Assert.assertEquals(handler.getExpirationPeriod(), Period.ofDays(90));
    Assert.assertEquals(handler.getWarningPeriod(), Period.ofDays(15));
    Assert.assertNull(
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
    Assert.assertNotNull(aggregateAuthenticator);
    Assert.assertTrue(aggregateAuthenticator.getDnResolver() instanceof AggregateDnResolver);
    final AggregateDnResolver dnResolvers = (AggregateDnResolver) aggregateAuthenticator.getDnResolver();
    for (DnResolver dnResolver : dnResolvers.getDnResolvers().values()) {
      testSearchDnResolver((SearchDnResolver) dnResolver, null);
    }

    Assert.assertNotNull(aggregateAuthenticator.getEntryResolver());

    Assert.assertTrue(
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
          Assert.assertNotNull(handler);
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
    Assert.assertNotNull(pooledConnectionFactory);
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
    Assert.assertNotNull(connectionFactory);
    testConnectionConfig(connectionFactory.getConnectionConfig(), null);
  }


  /**
   * Test search operation.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperation()
  {
    final SearchOperation operation = context.getBean("search-operation", SearchOperation.class);
    Assert.assertNotNull(operation);
    testSearchRequest(operation.getRequest());
  }


  /**
   * Test search operation worker.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperationWorker()
  {
    final SearchOperationWorker operation = context.getBean("search-operation-worker", SearchOperationWorker.class);
    Assert.assertNotNull(operation);
    testSearchRequest(operation.getOperation().getRequest());
  }


  /**
   * Test connection config.
   */
  @Test(groups = "beans-spring")
  public void testConnectionConfig()
  {
    final ConnectionConfig config = context.getBean("connection-config", ConnectionConfig.class);
    Assert.assertNotNull(config);
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
      testConnectionConfig(authHandler.getConnectionFactory().getConnectionConfig(), null);
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
    Assert.assertNotNull(dnResolver.getBaseDn());
    Assert.assertEquals(dnResolver.getUserFilter(), "(mail={user})");
    if (dnResolver.getConnectionFactory() instanceof PooledConnectionFactory) {
      testPooledConnectionFactory((PooledConnectionFactory) dnResolver.getConnectionFactory(), authType);
    } else {
      testConnectionConfig(
        dnResolver.getConnectionFactory().getConnectionConfig(), authType);
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
    Assert.assertEquals(factory.getBlockWaitTime(), Duration.ofMinutes(1));
    Assert.assertFalse(factory.getFailFastInitialize());
    Assert.assertEquals(factory.getPruneStrategy().getPrunePeriod(), Duration.ofMinutes(5));
    Assert.assertEquals(((IdlePruneStrategy) factory.getPruneStrategy()).getIdleTime(), Duration.ofMinutes(10));
    Assert.assertEquals(factory.getValidator().getClass(), SearchConnectionValidator.class);

    Assert.assertEquals(factory.getMinPoolSize(), 3);
    Assert.assertEquals(factory.getMaxPoolSize(), 10);
    Assert.assertEquals(factory.getValidator().getValidatePeriod(), Duration.ofMinutes(5));
    Assert.assertFalse(factory.isValidateOnCheckOut());
    Assert.assertTrue(factory.isValidatePeriodically());

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
    Assert.assertNotNull(connectionConfig.getLdapUrl());
    Assert.assertTrue(connectionConfig.getUseStartTLS());
    Assert.assertEquals(connectionConfig.getConnectTimeout(), Duration.ofSeconds(5));
    Assert.assertEquals(connectionConfig.getResponseTimeout(), Duration.ofSeconds(5));
    Assert.assertEquals(connectionConfig.getReconnectTimeout(), Duration.ofMinutes(1));
    Assert.assertFalse(connectionConfig.getAutoReconnect());
    Assert.assertFalse(connectionConfig.getAutoReplay());
    Assert.assertEquals(connectionConfig.getAutoReconnectCondition().getClass(), BackoffAutoReconnect.class);
    Assert.assertEquals(connectionConfig.getConnectionStrategy().getClass(), RoundRobinConnectionStrategy.class);
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      Assert.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      Assert.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }

    if (authType != null) {
      final BindConnectionInitializer ci = connectionConfig.getConnectionInitializers() != null ?
        (BindConnectionInitializer) connectionConfig.getConnectionInitializers()[0] : null;
      switch(authType) {
      case ANON_SEARCH:
      case DIRECT:
        Assert.assertNull(ci);
        break;
      case BIND_SEARCH:
      case AD:
        Assert.assertNotNull(ci);
        Assert.assertNotNull(ci.getBindDn());
        Assert.assertNotNull(ci.getBindCredential());
        break;
      case SASL_SEARCH:
        Assert.assertNotNull(ci);
        final SaslConfig sc = ci.getBindSaslConfig();
        Assert.assertNotNull(sc);
        Assert.assertEquals(sc.getMechanism(), Mechanism.DIGEST_MD5);
        Assert.assertEquals(sc.getQualityOfProtection()[0], QualityOfProtection.AUTH_INT);
        Assert.assertEquals(sc.getSecurityStrength(), SecurityStrength.MEDIUM);
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
    Assert.assertNotNull(request.getBaseDn());
    Assert.assertTrue(request.getBaseDn().length() > 0);
    Assert.assertNotNull(request.getFilter());
    Assert.assertTrue(request.getReturnAttributes().length > 0);
    Assert.assertEquals(request.getSearchScope(), SearchScope.ONELEVEL);
    Assert.assertEquals(request.getTimeLimit(), Duration.ofSeconds(5));
    Assert.assertEquals(request.getSizeLimit(), 10);
    Assert.assertTrue(request.getBinaryAttributes().length > 0);
  }
}
