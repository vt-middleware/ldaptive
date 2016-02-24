/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import java.time.Period;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchScope;
import org.ldaptive.SortBehavior;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.ldaptive.pool.SearchValidator;
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
    }
    final DnResolver dnResolver = auth.getDnResolver();
    if (dnResolver instanceof PooledConnectionFactoryManager) {
      ((PooledConnectionFactoryManager) dnResolver).getConnectionFactory().getConnectionPool().close();
    }
    final EntryResolver entryResolver = auth.getEntryResolver();
    if (entryResolver instanceof PooledConnectionFactoryManager) {
      ((PooledConnectionFactoryManager) entryResolver).getConnectionFactory().getConnectionPool().close();
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
    AssertJUnit.assertEquals(5, context.getBeansOfType(Authenticator.class).size());
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
    testSearchDnResolver(anonSearchAuthenticator);
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
    testSearchDnResolver(bindSearchAuthenticator);
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
   * Test sasl bind search authenticator.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testSaslBindSearchAuthenticator()
    throws Exception
  {
    final Authenticator saslBindSearchAuthenticator = context.getBean("sasl-auth", Authenticator.class);
    AssertJUnit.assertNotNull(saslBindSearchAuthenticator);
    testBindConnectionPool(saslBindSearchAuthenticator);
    testSearchDnResolver(saslBindSearchAuthenticator);
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
    testSearchDnResolver(adAuthenticator);
    AssertJUnit.assertNotNull(adAuthenticator.getAuthenticationResponseHandlers());
    AssertJUnit.assertEquals(
      ActiveDirectoryAuthenticationResponseHandler.class,
      adAuthenticator.getAuthenticationResponseHandlers()[0].getClass());
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
    testConnectionPool((BlockingConnectionPool) pooledConnectionFactory.getConnectionPool());
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
    testConnectionConfig(connectionFactory.getConnectionConfig());
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
    testSearchExecutor(executor);
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
    testConnectionPool(connectionPool);
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
    testConnectionConfig(config);
  }


  /**
   * Runs asserts against the bind connection pool.
   *
   * @param  auth  authenticator containing a bind connection pool
   */
  private void testBindConnectionPool(final Authenticator auth)
  {
    final PooledBindAuthenticationHandler authHandler =
      (PooledBindAuthenticationHandler) auth.getAuthenticationHandler();
    final BlockingConnectionPool pool = (BlockingConnectionPool) authHandler.getConnectionFactory().getConnectionPool();
    testConnectionPool(pool);
  }


  /**
   * Runs asserts against the DN resolver.
   *
   * @param  auth  authenticator containing a DN resolver
   */
  private void testSearchDnResolver(final Authenticator auth)
  {
    final PooledSearchDnResolver dnResolver = (PooledSearchDnResolver) auth.getDnResolver();
    AssertJUnit.assertNotNull(dnResolver.getBaseDn());
    AssertJUnit.assertEquals("(mail={user})", dnResolver.getUserFilter());
    final BlockingConnectionPool pool = (BlockingConnectionPool) dnResolver.getConnectionFactory().getConnectionPool();
    testConnectionPool(pool);
  }


  /**
   * Runs asserts against the connection pool.
   *
   * @param  pool  to test
   */
  private void testConnectionPool(final BlockingConnectionPool pool)
  {
    AssertJUnit.assertEquals(3000, pool.getBlockWaitTime());
    AssertJUnit.assertFalse(pool.getFailFastInitialize());
    AssertJUnit.assertEquals(300, pool.getPruneStrategy().getPrunePeriod());
    AssertJUnit.assertEquals(600, ((IdlePruneStrategy) pool.getPruneStrategy()).getIdleTime());
    AssertJUnit.assertEquals(SearchValidator.class, pool.getValidator().getClass());

    final PoolConfig poolConfig = pool.getPoolConfig();
    AssertJUnit.assertEquals(3, poolConfig.getMinPoolSize());
    AssertJUnit.assertEquals(10, poolConfig.getMaxPoolSize());
    AssertJUnit.assertEquals(300, poolConfig.getValidatePeriod());
    AssertJUnit.assertFalse(poolConfig.isValidateOnCheckOut());
    AssertJUnit.assertTrue(poolConfig.isValidatePeriodically());

    Connection conn = null;
    try {
      conn = pool.getConnection();
      testConnectionConfig(conn.getConnectionConfig());
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
   */
  private void testConnectionConfig(final ConnectionConfig connectionConfig)
  {
    AssertJUnit.assertNotNull(connectionConfig.getLdapUrl());
    AssertJUnit.assertTrue(connectionConfig.getUseStartTLS());
    AssertJUnit.assertFalse(connectionConfig.getUseSSL());
    AssertJUnit.assertEquals(3000, connectionConfig.getConnectTimeout());
    final CredentialConfig credentialConfig =  connectionConfig.getSslConfig().getCredentialConfig();
    if (credentialConfig instanceof X509CredentialConfig) {
      AssertJUnit.assertNotNull(((X509CredentialConfig) credentialConfig).getTrustCertificates());
    } else if (credentialConfig instanceof KeyStoreCredentialConfig) {
      AssertJUnit.assertNotNull(((KeyStoreCredentialConfig) credentialConfig).getTrustStore());
    }
  }


  /**
   * Runs asserts against the search executor.
   *
   * @param  executor  to test
   */
  private void testSearchExecutor(final SearchExecutor executor)
  {
    AssertJUnit.assertNotNull(executor.getBaseDn());
    AssertJUnit.assertTrue(executor.getBaseDn().length() > 0);
    AssertJUnit.assertNotNull(executor.getSearchFilter());
    AssertJUnit.assertTrue(executor.getReturnAttributes().length > 0);
    AssertJUnit.assertEquals(SearchScope.ONELEVEL, executor.getSearchScope());
    AssertJUnit.assertEquals(5000, executor.getTimeLimit());
    AssertJUnit.assertEquals(10, executor.getSizeLimit());
    AssertJUnit.assertTrue(executor.getBinaryAttributes().length > 0);
    AssertJUnit.assertEquals(SortBehavior.ORDERED, executor.getSortBehavior());
  }
}
