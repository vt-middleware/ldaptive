/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Duration;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.concurrent.SearchOperationWorker;
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
    AssertJUnit.assertEquals(1, context.getBeansOfType(PooledConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(DefaultConnectionFactory.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(SearchOperation.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(SearchOperationWorker.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(ConnectionConfig.class).size());
    AssertJUnit.assertEquals(1, context.getBeansOfType(Authenticator.class).size());
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
   * Test parallel search operation.
   */
  @Test(groups = "beans-spring")
  public void testSearchOperationWorker()
  {
    final SearchOperationWorker operation = context.getBean(
      "search-operation-worker", SearchOperationWorker.class);
    AssertJUnit.assertNotNull(operation);
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
    AssertJUnit.assertEquals(Duration.ofSeconds(7), connectionConfig.getResponseTimeout());
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      AssertJUnit.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      AssertJUnit.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }

    if (authType != null) {
      final BindConnectionInitializer ci = (BindConnectionInitializer) connectionConfig.getConnectionInitializers()[0];
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
    AssertJUnit.assertNotNull(request.getFilter());
    AssertJUnit.assertTrue(request.getReturnAttributes().length > 0);
    AssertJUnit.assertEquals(SearchScope.ONELEVEL, request.getSearchScope());
    AssertJUnit.assertEquals(Duration.ofSeconds(5), request.getTimeLimit());
    AssertJUnit.assertEquals(10, request.getSizeLimit());
    AssertJUnit.assertTrue(request.getBinaryAttributes().length > 0);
  }
}
