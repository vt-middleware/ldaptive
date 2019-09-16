/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Function;
import javax.security.auth.login.LoginContext;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.jaas.RoleResolver;
import org.ldaptive.jaas.TestCallbackHandler;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.SearchRequestPropertySource;
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
  @BeforeClass(groups = "props")
  public void init()
  {
    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "props")
  public void nullProperties()
    throws Exception
  {
    final ConnectionConfig cc = new ConnectionConfig();
    final ConnectionConfigPropertySource ccSource = new ConnectionConfigPropertySource(
      cc,
      "classpath:/org/ldaptive/ldap.null.properties");
    ccSource.initialize();

    AssertJUnit.assertNotNull(cc.getConnectionInitializers());
    AssertJUnit.assertNull(((BindConnectionInitializer) cc.getConnectionInitializers()[0]).getBindSaslConfig());

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.null.properties");
    srSource.initialize();
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
  @Test(groups = "props")
  public void parserProperties(final String bindDn, final String host)
    throws Exception
  {
    final DefaultConnectionFactory cf = new DefaultConnectionFactory();
    final DefaultConnectionFactoryPropertySource cfSource = new DefaultConnectionFactoryPropertySource(
      cf,
      "classpath:/org/ldaptive/ldap.parser.properties");
    cfSource.initialize();

    final ConnectionConfig cc = cf.getConnectionConfig();
    final BindConnectionInitializer ci = (BindConnectionInitializer) cc.getConnectionInitializers()[0];

    AssertJUnit.assertEquals(host, cc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, ci.getBindDn());
    AssertJUnit.assertEquals(Duration.ofSeconds(8), cc.getConnectTimeout());
    AssertJUnit.assertFalse(cc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, cc.getConnectionStrategy().getClass());

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.parser.properties");
    srSource.initialize();

    AssertJUnit.assertEquals(DnParser.substring(bindDn, 1).toLowerCase(), sr.getBaseDn().toLowerCase());
    AssertJUnit.assertEquals(SearchScope.OBJECT, sr.getSearchScope());
    AssertJUnit.assertEquals(Duration.ofSeconds(5), sr.getTimeLimit());
    AssertJUnit.assertEquals("jpegPhoto", sr.getBinaryAttributes()[0]);
    AssertJUnit.assertEquals(5, ((PagedResultsControl) sr.getControls()[0]).getSize());

    final Authenticator auth = new Authenticator();
    final AuthenticatorPropertySource aSource = new AuthenticatorPropertySource(
      auth,
      "classpath:/org/ldaptive/ldap.parser.properties");
    aSource.initialize();

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (MergeAttributeEntryHandler.class.isInstance(handler)) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        AssertJUnit.assertNotNull(h);
      } else if (DnAttributeEntryHandler.class.isInstance(handler)) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        AssertJUnit.assertEquals("myDN", h.getDnAttributeName());
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    AssertJUnit.assertEquals(1, resolverCf.getPoolConfig().getMinPoolSize());
    AssertJUnit.assertEquals(3, resolverCf.getPoolConfig().getMaxPoolSize());
    AssertJUnit.assertEquals(true, resolverCf.getPoolConfig().isValidatePeriodically());
    AssertJUnit.assertNotNull(resolverCf.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    AssertJUnit.assertEquals(Duration.ofMinutes(1), pruneStrategy.getPrunePeriod());
    AssertJUnit.assertEquals(Duration.ofMinutes(2), pruneStrategy.getIdleTime());
    AssertJUnit.assertNotNull(resolverCf.getActivator());
    AssertJUnit.assertNotNull(resolverCf.getPassivator());

    final DefaultConnectionFactory resolverBaseCf = resolverCf.getDefaultConnectionFactory();
    final ConnectionConfig authCc = resolverBaseCf.getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    AssertJUnit.assertEquals("ldap://auth.ldaptive.org:14389", authCc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, authCi.getBindDn());
    AssertJUnit.assertEquals(Duration.ofSeconds(8), authCc.getConnectTimeout());
    AssertJUnit.assertTrue(authCc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, authCc.getConnectionStrategy().getClass());

    if (auth.getDnResolver() instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager dnResolverCfm = (ConnectionFactoryManager) auth.getDnResolver();
      dnResolverCfm.getConnectionFactory().close();
    }

    if (auth.getAuthenticationHandler() instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager authHandlerCfm = (ConnectionFactoryManager) auth.getAuthenticationHandler();
      authHandlerCfm.getConnectionFactory().close();
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
  @Test(groups = "props")
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
        if (roleResolver instanceof ConnectionFactoryManager) {
          final ConnectionFactoryManager roleResolverCfm = (ConnectionFactoryManager) roleResolver;
          roleResolverCfm.getConnectionFactory().close();
        }
      } else if (o instanceof SearchRequest) {
        searchRequest = (SearchRequest) o;
      } else {
        throw new Exception("Unknown public credential found: " + o);
      }
    }

    final ConnectionFactoryManager cfm = (ConnectionFactoryManager) auth.getAuthenticationHandler();
    final PooledConnectionFactory cf = (PooledConnectionFactory) cfm.getConnectionFactory();
    final ConnectionConfig cc = cf.getConnectionConfig();
    final BindConnectionInitializer ci = (BindConnectionInitializer) cc.getConnectionInitializers()[0];

    AssertJUnit.assertNotNull(cf.getProvider().getClass());
    AssertJUnit.assertEquals(host, cc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, ci.getBindDn());
    AssertJUnit.assertEquals(Duration.ofSeconds(8), cc.getConnectTimeout());
    AssertJUnit.assertTrue(cc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, cc.getConnectionStrategy().getClass());

    AssertJUnit.assertEquals(DnParser.substring(bindDn, 1).toLowerCase(), searchRequest.getBaseDn().toLowerCase());
    AssertJUnit.assertEquals(SearchScope.OBJECT, searchRequest.getSearchScope());
    AssertJUnit.assertEquals(Duration.ofSeconds(5), searchRequest.getTimeLimit());
    AssertJUnit.assertEquals("jpegPhoto", searchRequest.getBinaryAttributes()[0]);

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (MergeAttributeEntryHandler.class.isInstance(handler)) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        AssertJUnit.assertNotNull(h);
      } else if (DnAttributeEntryHandler.class.isInstance(handler)) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        AssertJUnit.assertEquals("myDN", h.getDnAttributeName());
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    AssertJUnit.assertEquals(1, resolverCf.getPoolConfig().getMinPoolSize());
    AssertJUnit.assertEquals(3, resolverCf.getPoolConfig().getMaxPoolSize());
    AssertJUnit.assertEquals(true, resolverCf.getPoolConfig().isValidatePeriodically());
    AssertJUnit.assertNotNull(resolverCf.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    AssertJUnit.assertEquals(Duration.ofMinutes(1), pruneStrategy.getPrunePeriod());
    AssertJUnit.assertEquals(Duration.ofMinutes(2), pruneStrategy.getIdleTime());

    final ConnectionConfig authCc = resolverCf.getDefaultConnectionFactory().getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    AssertJUnit.assertEquals(host, authCc.getLdapUrl());
    AssertJUnit.assertEquals(bindDn, authCi.getBindDn());
    AssertJUnit.assertEquals(Duration.ofSeconds(8), authCc.getConnectTimeout());
    AssertJUnit.assertTrue(authCc.getUseStartTLS());
    AssertJUnit.assertEquals(RoundRobinConnectionStrategy.class, authCc.getConnectionStrategy().getClass());

    AssertJUnit.assertEquals(
      org.ldaptive.auth.CompareAuthenticationHandler.class,
      auth.getAuthenticationHandler().getClass());
    AssertJUnit.assertEquals(org.ldaptive.auth.SearchDnResolver.class, auth.getDnResolver().getClass());

    if (auth.getDnResolver() instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager dnResolverCfm = (ConnectionFactoryManager) auth.getDnResolver();
      dnResolverCfm.getConnectionFactory().close();
    }

    if (auth.getAuthenticationHandler() instanceof ConnectionFactoryManager) {
      final ConnectionFactoryManager authHandlerCfm = (ConnectionFactoryManager) auth.getAuthenticationHandler();
      authHandlerCfm.getConnectionFactory().close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "props")
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
  }
}
