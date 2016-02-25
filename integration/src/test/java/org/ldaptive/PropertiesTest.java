/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import javax.security.auth.login.LoginContext;
import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.PooledSearchDnResolver;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.RecursiveEntryHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.jaas.RoleResolver;
import org.ldaptive.jaas.TestCallbackHandler;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.provider.Provider;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for property source implementations in the props package.
 *
 * @author  Middleware Services
 */
public class PropertiesTest
{


  /** @throws  Exception  On test failure. */
  @BeforeClass(groups = {"props"})
  public void init()
    throws Exception
  {
    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"props"})
  public void nullProperties()
    throws Exception
  {
    final ConnectionConfig cc = new ConnectionConfig();
    final ConnectionConfigPropertySource ccSource = new ConnectionConfigPropertySource(
      cc,
      "classpath:/org/ldaptive/ldap.null.properties");
    ccSource.initialize();

    AssertJUnit.assertNotNull(cc.getConnectionInitializer());
    AssertJUnit.assertNull(((BindConnectionInitializer) cc.getConnectionInitializer()).getBindSaslConfig());

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.null.properties");
    srSource.initialize();

    AssertJUnit.assertNull(sr.getSearchEntryHandlers());
  }


  /**
   * @param  bindDn  used to make connections
   * @param  host  that should match a property.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldapBindDn",
      "ldapTestHost"
    })
  @Test(groups = {"props"})
  public void parserProperties(final String bindDn, final String host)
    throws Exception
  {
    final DefaultConnectionFactory cf = new DefaultConnectionFactory();
    final DefaultConnectionFactoryPropertySource cfSource = new DefaultConnectionFactoryPropertySource(
      cf,
      "classpath:/org/ldaptive/ldap.parser.properties");
    cfSource.initialize();

    final ConnectionConfig cc = cf.getConnectionConfig();
    final BindConnectionInitializer ci = (BindConnectionInitializer) cc.getConnectionInitializer();

    AssertJUnit.assertEquals(host, cc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, ci.getBindDn());
    AssertJUnit.assertEquals(8000, cc.getConnectTimeout());
    AssertJUnit.assertFalse(cc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, cc.getConnectionStrategy().getClass());
    AssertJUnit.assertEquals(
      org.ldaptive.provider.TestControlProcessor.class,
      cf.getProvider().getProviderConfig().getControlProcessor().getClass());
    AssertJUnit.assertEquals(1, cf.getProvider().getProviderConfig().getProperties().size());
    AssertJUnit.assertEquals(
      "true",
      cf.getProvider().getProviderConfig().getProperties().get("java.naming.authoritative"));

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.parser.properties");
    srSource.initialize();

    AssertJUnit.assertEquals(DnParser.substring(bindDn, 1).toLowerCase(), sr.getBaseDn().toLowerCase());
    AssertJUnit.assertEquals(SearchScope.OBJECT, sr.getSearchScope());
    AssertJUnit.assertEquals(5000, sr.getTimeLimit());
    AssertJUnit.assertEquals("jpegPhoto", sr.getBinaryAttributes()[0]);
    AssertJUnit.assertEquals(5, ((PagedResultsControl) sr.getControls()[0]).getSize());

    for (SearchEntryHandler rh : sr.getSearchEntryHandlers()) {
      if (RecursiveEntryHandler.class.isInstance(rh)) {
        final RecursiveEntryHandler h = (RecursiveEntryHandler) rh;
        AssertJUnit.assertEquals("member", h.getSearchAttribute());
        AssertJUnit.assertEquals(
          Arrays.asList(new String[] {"mail", "department"}),
          Arrays.asList(h.getMergeAttributes()));
      } else if (MergeAttributeEntryHandler.class.isInstance(rh)) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) rh;
        AssertJUnit.assertNotNull(h);
      } else if (DnAttributeEntryHandler.class.isInstance(rh)) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) rh;
        AssertJUnit.assertEquals("myDN", h.getDnAttributeName());
      } else {
        throw new Exception("Unknown search result handler type " + rh);
      }
    }

    final Authenticator auth = new Authenticator();
    final AuthenticatorPropertySource aSource = new AuthenticatorPropertySource(
      auth,
      "classpath:/org/ldaptive/ldap.parser.properties");
    aSource.initialize();

    final PooledSearchDnResolver dnResolver = (PooledSearchDnResolver) auth.getDnResolver();
    final BlockingConnectionPool authCp =
      (BlockingConnectionPool) dnResolver.getConnectionFactory().getConnectionPool();
    AssertJUnit.assertEquals(1, authCp.getPoolConfig().getMinPoolSize());
    AssertJUnit.assertEquals(3, authCp.getPoolConfig().getMaxPoolSize());
    AssertJUnit.assertEquals(true, authCp.getPoolConfig().isValidatePeriodically());
    AssertJUnit.assertNotNull(authCp.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) authCp.getPruneStrategy();
    AssertJUnit.assertEquals(60, pruneStrategy.getPrunePeriod());
    AssertJUnit.assertEquals(120, pruneStrategy.getIdleTime());
    AssertJUnit.assertNotNull(authCp.getActivator());
    AssertJUnit.assertNotNull(authCp.getPassivator());

    final DefaultConnectionFactory authCf = authCp.getConnectionFactory();
    final ConnectionConfig authCc = authCf.getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializer();
    AssertJUnit.assertEquals("ldap://ed-auth.middleware.vt.edu:14389", authCc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, authCi.getBindDn());
    AssertJUnit.assertEquals(8000, authCc.getConnectTimeout());
    AssertJUnit.assertTrue(authCc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, authCc.getConnectionStrategy().getClass());
    AssertJUnit.assertEquals(1, authCf.getProvider().getProviderConfig().getProperties().size());
    AssertJUnit.assertEquals(
      "true",
      authCf.getProvider().getProviderConfig().getProperties().get("java.naming.authoritative"));

    if (auth.getDnResolver() instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm = (PooledConnectionFactoryManager) auth.getDnResolver();
      cfm.getConnectionFactory().getConnectionPool().close();
    }

    final AuthenticationHandler ah = auth.getAuthenticationHandler();
    if (ah instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager cfm = (PooledConnectionFactoryManager) ah;
      cfm.getConnectionFactory().getConnectionPool().close();
    }
  }


  /**
   * @param  bindDn  used to make connections
   * @param  host  that should match a property.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "ldapBindDn",
      "ldapTestHost"
    })
  @Test(groups = {"props"})
  public void jaasProperties(final String bindDn, final String host)
    throws Exception
  {
    final LoginContext lc = new LoginContext("ldaptive-props", new TestCallbackHandler());
    lc.login();

    Authenticator auth = null;
    AuthenticationRequest authRequest = null;
    RoleResolver roleResolver = null;
    SearchRequest searchRequest = null;
    for (Object o : lc.getSubject().getPublicCredentials()) {
      if (o instanceof Authenticator) {
        auth = (Authenticator) o;
      } else if (o instanceof AuthenticationRequest) {
        authRequest = (AuthenticationRequest) o;
      } else if (o instanceof RoleResolver) {
        roleResolver = (RoleResolver) o;
      } else if (o instanceof SearchRequest) {
        searchRequest = (SearchRequest) o;
      } else {
        throw new Exception("Unknown public credential found: " + o);
      }
    }

    final ConnectionFactoryManager cfm = (ConnectionFactoryManager) auth.getAuthenticationHandler();
    final DefaultConnectionFactory cf = (DefaultConnectionFactory) cfm.getConnectionFactory();
    final ConnectionConfig cc = cf.getConnectionConfig();
    final BindConnectionInitializer ci = (BindConnectionInitializer) cc.getConnectionInitializer();

    AssertJUnit.assertNotNull(cf.getProvider().getClass());
    AssertJUnit.assertEquals(host, cc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, ci.getBindDn());
    AssertJUnit.assertEquals(8000, cc.getConnectTimeout());
    AssertJUnit.assertTrue(cc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, cc.getConnectionStrategy().getClass());
    AssertJUnit.assertEquals(1, cf.getProvider().getProviderConfig().getProperties().size());
    AssertJUnit.assertEquals(
      "true",
      cf.getProvider().getProviderConfig().getProperties().get("java.naming.authoritative"));

    AssertJUnit.assertEquals(DnParser.substring(bindDn, 1).toLowerCase(), searchRequest.getBaseDn().toLowerCase());
    AssertJUnit.assertEquals(SearchScope.OBJECT, searchRequest.getSearchScope());
    AssertJUnit.assertEquals(5000, searchRequest.getTimeLimit());
    AssertJUnit.assertEquals("jpegPhoto", searchRequest.getBinaryAttributes()[0]);

    for (SearchEntryHandler srh : searchRequest.getSearchEntryHandlers()) {
      if (RecursiveEntryHandler.class.isInstance(srh)) {
        final RecursiveEntryHandler h = (RecursiveEntryHandler) srh;
        AssertJUnit.assertEquals("member", h.getSearchAttribute());
        AssertJUnit.assertEquals(
          Arrays.asList(new String[] {"mail", "department"}),
          Arrays.asList(h.getMergeAttributes()));
      } else if (MergeAttributeEntryHandler.class.isInstance(srh)) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) srh;
        AssertJUnit.assertNotNull(h);
      } else if (DnAttributeEntryHandler.class.isInstance(srh)) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) srh;
        AssertJUnit.assertEquals("myDN", h.getDnAttributeName());
      } else {
        throw new Exception("Unknown search result handler type " + srh);
      }
    }

    final PooledConnectionFactory authCf = ((PooledSearchDnResolver) auth.getDnResolver()).getConnectionFactory();
    final BlockingConnectionPool authCp = (BlockingConnectionPool) authCf.getConnectionPool();
    AssertJUnit.assertEquals(1, authCp.getPoolConfig().getMinPoolSize());
    AssertJUnit.assertEquals(3, authCp.getPoolConfig().getMaxPoolSize());
    AssertJUnit.assertEquals(true, authCp.getPoolConfig().isValidatePeriodically());
    AssertJUnit.assertNotNull(authCp.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) authCp.getPruneStrategy();
    AssertJUnit.assertEquals(60, pruneStrategy.getPrunePeriod());
    AssertJUnit.assertEquals(120, pruneStrategy.getIdleTime());

    final ConnectionConfig authCc = authCp.getConnectionFactory().getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializer();
    final Provider<?> authP = authCp.getConnectionFactory().getProvider();
    AssertJUnit.assertEquals(host, authCc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, authCi.getBindDn());
    AssertJUnit.assertEquals(8000, authCc.getConnectTimeout());
    AssertJUnit.assertTrue(authCc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, authCc.getConnectionStrategy().getClass());
    AssertJUnit.assertEquals(1, authP.getProviderConfig().getProperties().size());
    AssertJUnit.assertEquals("true", authP.getProviderConfig().getProperties().get("java.naming.authoritative"));

    AssertJUnit.assertEquals(
      org.ldaptive.auth.CompareAuthenticationHandler.class,
      auth.getAuthenticationHandler().getClass());
    AssertJUnit.assertEquals(org.ldaptive.auth.PooledSearchDnResolver.class, auth.getDnResolver().getClass());

    if (auth.getDnResolver() instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager resolverCfm = (PooledConnectionFactoryManager) auth.getDnResolver();
      resolverCfm.getConnectionFactory().getConnectionPool().close();
    }

    final AuthenticationHandler authHandler = auth.getAuthenticationHandler();
    if (authHandler instanceof PooledConnectionFactoryManager) {
      final PooledConnectionFactoryManager handlerCfm = (PooledConnectionFactoryManager) authHandler;
      handlerCfm.getConnectionFactory().getConnectionPool().close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"props"})
  public void multipleProperties()
    throws Exception
  {
    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.parser.properties",
      "classpath:/org/ldaptive/ldap.null.properties");
    srSource.initialize();

    AssertJUnit.assertEquals(SearchScope.SUBTREE, sr.getSearchScope());
    AssertJUnit.assertNotNull(sr.getControls());
    AssertJUnit.assertNull(sr.getSearchEntryHandlers());
  }
}
