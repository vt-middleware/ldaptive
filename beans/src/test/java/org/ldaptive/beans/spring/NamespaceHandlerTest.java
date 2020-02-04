/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.concurrent.SearchOperationWorker;
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


  /**
   * Attempts to load a Spring application context XML files to verify proper wiring.
   */
  @Test(groups = "beans-spring")
  public void testSpringWiring()
  {
    Assert.assertEquals(context.getBeansOfType(PooledConnectionFactory.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(DefaultConnectionFactory.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(SearchOperation.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(SearchOperationWorker.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(ConnectionConfig.class).size(), 1);
    Assert.assertEquals(context.getBeansOfType(Authenticator.class).size(), 1);
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
   * Test parallel search operation.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperationWorker()
  {
    final SearchOperationWorker operation = context.getBean(
      "search-operation-worker", SearchOperationWorker.class);
    Assert.assertNotNull(operation);
    testSearchRequest(operation.getOperation().getRequest());
  }


  /**
   * Runs asserts against a pooled connection factory.
   *
   * @param  factory  to test
   * @param  authType  authenticator type or null
   */
  private void testPooledConnectionFactory(final PooledConnectionFactory factory, final AuthenticatorType authType)
  {
    Assert.assertEquals(factory.getBlockWaitTime(), Duration.ofSeconds(3));
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
    Assert.assertEquals(connectionConfig.getConnectTimeout(), Duration.ofSeconds(3));
    Assert.assertEquals(connectionConfig.getResponseTimeout(), Duration.ofSeconds(7));
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      Assert.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      Assert.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }

    if (authType != null) {
      final BindConnectionInitializer ci = (BindConnectionInitializer) connectionConfig.getConnectionInitializers()[0];
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
        Assert.assertEquals(sc.getQualityOfProtection(), QualityOfProtection.AUTH_INT);
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
