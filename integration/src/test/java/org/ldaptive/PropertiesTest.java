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
import org.ldaptive.pool.BindConnectionPassivator;
import org.ldaptive.pool.IdlePruneStrategy;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.SearchRequestPropertySource;
import org.testng.Assert;
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
    throws Exception
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

    Assert.assertNotNull(cc.getConnectionInitializers());
    Assert.assertNull(((BindConnectionInitializer) cc.getConnectionInitializers()[0]).getBindSaslConfig());

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

    Assert.assertEquals(cc.getLdapUrl(), host);
    Assert.assertEquals(ci.getBindDn(), bindDn);
    Assert.assertEquals(cc.getConnectTimeout(), Duration.ofSeconds(8));
    Assert.assertFalse(cc.getUseStartTLS());
    Assert.assertEquals(cc.getConnectionStrategy().getClass(), RoundRobinConnectionStrategy.class);

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.parser.properties");
    srSource.initialize();

    Assert.assertEquals(sr.getBaseDn().toLowerCase(), DnParser.substring(bindDn, 1).toLowerCase());
    Assert.assertEquals(sr.getSearchScope(), SearchScope.OBJECT);
    Assert.assertEquals(sr.getTimeLimit(), Duration.ofSeconds(5));
    Assert.assertEquals(sr.getBinaryAttributes()[0], "jpegPhoto");
    Assert.assertEquals(((PagedResultsControl) sr.getControls()[0]).getSize(), 5);

    final Authenticator auth = new Authenticator();
    final AuthenticatorPropertySource aSource = new AuthenticatorPropertySource(
      auth,
      "classpath:/org/ldaptive/ldap.parser.properties");
    aSource.initialize();

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (handler instanceof MergeAttributeEntryHandler) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        Assert.assertNotNull(h);
      } else if (handler instanceof DnAttributeEntryHandler) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        Assert.assertEquals(h.getDnAttributeName(), "myDN");
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    Assert.assertEquals(resolverCf.getMinPoolSize(), 1);
    Assert.assertEquals(resolverCf.getMaxPoolSize(), 3);
    Assert.assertEquals(resolverCf.isValidatePeriodically(), true);
    Assert.assertNotNull(resolverCf.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    Assert.assertEquals(pruneStrategy.getPrunePeriod(), Duration.ofMinutes(1));
    Assert.assertEquals(pruneStrategy.getIdleTime(), Duration.ofMinutes(2));
    Assert.assertNotNull(resolverCf.getActivator());
    Assert.assertEquals(resolverCf.getPassivator().getClass(), BindConnectionPassivator.class);

    final DefaultConnectionFactory resolverBaseCf = resolverCf.getDefaultConnectionFactory();
    final ConnectionConfig authCc = resolverBaseCf.getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    Assert.assertEquals(authCc.getLdapUrl(), "ldap://auth.ldaptive.org:14389");
    Assert.assertEquals(authCi.getBindDn(), bindDn);
    Assert.assertEquals(authCc.getConnectTimeout(), Duration.ofSeconds(8));
    Assert.assertTrue(authCc.getUseStartTLS());
    Assert.assertEquals(authCc.getConnectionStrategy().getClass(), RoundRobinConnectionStrategy.class);

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

    Assert.assertNotNull(cf.getTransport().getClass());
    Assert.assertEquals(cc.getLdapUrl(), host);
    Assert.assertEquals(ci.getBindDn(), bindDn);
    Assert.assertEquals(cc.getConnectTimeout(), Duration.ofSeconds(8));
    Assert.assertTrue(cc.getUseStartTLS());
    Assert.assertEquals(cc.getConnectionStrategy().getClass(), RoundRobinConnectionStrategy.class);

    Assert.assertEquals(searchRequest.getBaseDn().toLowerCase(), DnParser.substring(bindDn, 1).toLowerCase());
    Assert.assertEquals(searchRequest.getSearchScope(), SearchScope.OBJECT);
    Assert.assertEquals(searchRequest.getTimeLimit(), Duration.ofSeconds(5));
    Assert.assertEquals(searchRequest.getBinaryAttributes()[0], "jpegPhoto");

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (handler instanceof MergeAttributeEntryHandler) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        Assert.assertNotNull(h);
      } else if (handler instanceof DnAttributeEntryHandler) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        Assert.assertEquals(h.getDnAttributeName(), "myDN");
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    Assert.assertEquals(resolverCf.getMinPoolSize(), 1);
    Assert.assertEquals(resolverCf.getMaxPoolSize(), 3);
    Assert.assertEquals(resolverCf.isValidatePeriodically(), true);
    Assert.assertNotNull(resolverCf.getValidator());

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    Assert.assertEquals(pruneStrategy.getPrunePeriod(), Duration.ofMinutes(1));
    Assert.assertEquals(pruneStrategy.getIdleTime(), Duration.ofMinutes(2));

    final ConnectionConfig authCc = resolverCf.getDefaultConnectionFactory().getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    Assert.assertEquals(authCc.getLdapUrl(), host);
    Assert.assertEquals(authCi.getBindDn(), bindDn);
    Assert.assertEquals(authCc.getConnectTimeout(), Duration.ofSeconds(8));
    Assert.assertTrue(authCc.getUseStartTLS());
    Assert.assertEquals(authCc.getConnectionStrategy().getClass(), RoundRobinConnectionStrategy.class);

    Assert.assertEquals(
      auth.getAuthenticationHandler().getClass(),
      org.ldaptive.auth.CompareAuthenticationHandler.class);
    Assert.assertEquals(auth.getDnResolver().getClass(), org.ldaptive.auth.SearchDnResolver.class);

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

    Assert.assertEquals(sr.getSearchScope(), SearchScope.SUBTREE);
    Assert.assertNotNull(sr.getControls());
  }
}
