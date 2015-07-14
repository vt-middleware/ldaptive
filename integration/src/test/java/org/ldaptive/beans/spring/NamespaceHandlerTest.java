/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapException;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PoolConfig;
import org.ldaptive.pool.SearchValidator;
import org.ldaptive.ssl.X509CredentialConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Unit test for {@link NamespaceHandler}.
 *
 * @author  Middleware Services
 */
public class NamespaceHandlerTest
{


  /**
   * Attempts to load a Spring application context XML files to verify proper wiring.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-spring"})
  public void testSpringWiring()
    throws Exception
  {
    final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
      new String[] {"/spring-ext-context.xml", });
    AssertJUnit.assertEquals(4, context.getBeanDefinitionCount());

    final Authenticator anonSearchAuthenticator = context.getBean(
      "anonymous-search-authenticator",
      Authenticator.class);
    AssertJUnit.assertNotNull(anonSearchAuthenticator);
    testBindConnectionPool(anonSearchAuthenticator);
    testSearchDnResolver(anonSearchAuthenticator);

    final Authenticator bindSearchAuthenticator = context.getBean("bind-search-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(bindSearchAuthenticator);
    testBindConnectionPool(bindSearchAuthenticator);
    testSearchDnResolver(bindSearchAuthenticator);

    final Authenticator directAuthenticator = context.getBean("direct-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(directAuthenticator);
    testBindConnectionPool(directAuthenticator);
    AssertJUnit.assertNotNull(((FormatDnResolver) directAuthenticator.getDnResolver()).getFormat());

    final Authenticator adAuthenticator = context.getBean("ad-authenticator", Authenticator.class);
    AssertJUnit.assertNotNull(adAuthenticator);
    testBindConnectionPool(adAuthenticator);
    AssertJUnit.assertNotNull(((FormatDnResolver) adAuthenticator.getDnResolver()).getFormat());
    AssertJUnit.assertEquals(
      ActiveDirectoryAuthenticationResponseHandler.class,
      adAuthenticator.getAuthenticationResponseHandlers()[0].getClass());
  }


  private void testBindConnectionPool(final Authenticator auth)
  {
    final PooledBindAuthenticationHandler authHandler =
      (PooledBindAuthenticationHandler) auth.getAuthenticationHandler();
    final BlockingConnectionPool pool = (BlockingConnectionPool) authHandler.getConnectionFactory().getConnectionPool();
    testConnectionPool(pool);
  }


  private void testSearchDnResolver(final Authenticator auth)
  {
    final PooledSearchDnResolver dnResolver = (PooledSearchDnResolver) auth.getDnResolver();
    AssertJUnit.assertNotNull(dnResolver.getBaseDn());
    AssertJUnit.assertEquals("(mail={user})", dnResolver.getUserFilter());
    final BlockingConnectionPool pool = (BlockingConnectionPool) dnResolver.getConnectionFactory().getConnectionPool();
    testConnectionPool(pool);
  }


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
      final ConnectionConfig connectionConfig = conn.getConnectionConfig();
      AssertJUnit.assertNotNull(connectionConfig.getLdapUrl());
      AssertJUnit.assertTrue(connectionConfig.getUseStartTLS());
      AssertJUnit.assertFalse(connectionConfig.getUseSSL());
      AssertJUnit.assertEquals(3000, connectionConfig.getConnectTimeout());
      final X509CredentialConfig credentialConfig =
        (X509CredentialConfig) connectionConfig.getSslConfig().getCredentialConfig();
      AssertJUnit.assertNotNull(credentialConfig.getTrustCertificates());
    } catch (LdapException e) {
      AssertJUnit.fail("Error getting connection from pool: " + e.getMessage());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
}
