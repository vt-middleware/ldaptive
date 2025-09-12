/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import javax.security.auth.login.LoginContext;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.dn.Dn;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
    System.setProperty("java.security.auth.login.config", "integration/target/test-classes/ldap_jaas.config");
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

    assertThat(cc.getConnectionInitializers()).isNotNull();
    assertThat(((BindConnectionInitializer) cc.getConnectionInitializers()[0]).getBindSaslConfig()).isNull();

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
  @Parameters({"ldapBindDn", "ldapTestHost"})
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

    assertThat(cc.getLdapUrl()).isEqualTo(host);
    assertThat(ci.getBindDn()).isEqualTo(bindDn);
    assertThat(cc.getConnectTimeout()).isEqualTo(Duration.ofSeconds(8));
    assertThat(cc.getAutoRead()).isFalse();
    assertThat(cc.getUseStartTLS()).isFalse();
    assertThat(cc.getConnectionStrategy().getClass()).isEqualTo(RoundRobinConnectionStrategy.class);
    assertThat(cc.getTransportOptions())
      .isEqualTo(Map.of("SO_LINGER", "false", "TCP_NODELAY", "false", "SO_RCVBUF", "1024"));

    final SearchRequest sr = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      sr,
      "classpath:/org/ldaptive/ldap.parser.properties");
    srSource.initialize();

    assertThat(new Dn(sr.getBaseDn()).format()).isEqualTo(new Dn(bindDn).subDn(1).format());
    assertThat(sr.getSearchScope()).isEqualTo(SearchScope.OBJECT);
    assertThat(sr.getTimeLimit()).isEqualTo(Duration.ofSeconds(5));
    assertThat(sr.getBinaryAttributes()[0]).isEqualTo("jpegPhoto");
    assertThat(((PagedResultsControl) sr.getControls()[0]).getSize()).isEqualTo(5);

    final Authenticator auth = new Authenticator();
    final AuthenticatorPropertySource aSource = new AuthenticatorPropertySource(
      auth,
      "classpath:/org/ldaptive/ldap.parser.properties");
    aSource.initialize();

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (handler instanceof MergeAttributeEntryHandler) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        assertThat(h).isNotNull();
      } else if (handler instanceof DnAttributeEntryHandler) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        assertThat(h.getDnAttributeName()).isEqualTo("myDN");
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    assertThat(resolverCf.getMinPoolSize()).isEqualTo(1);
    assertThat(resolverCf.getMaxPoolSize()).isEqualTo(3);
    assertThat(resolverCf.isValidatePeriodically()).isTrue();
    assertThat(resolverCf.getValidator()).isNotNull();

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    assertThat(pruneStrategy.getPrunePeriod()).isEqualTo(Duration.ofMinutes(1));
    assertThat(pruneStrategy.getIdleTime()).isEqualTo(Duration.ofMinutes(2));
    assertThat(resolverCf.getActivator()).isNotNull();
    assertThat(resolverCf.getPassivator().getClass()).isEqualTo(BindConnectionPassivator.class);

    final DefaultConnectionFactory resolverBaseCf = resolverCf.getDefaultConnectionFactory();
    final ConnectionConfig authCc = resolverBaseCf.getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    assertThat(authCc.getLdapUrl()).isEqualTo("ldap://auth.ldaptive.org:14389");
    assertThat(authCi.getBindDn()).isEqualTo(bindDn);
    assertThat(authCc.getConnectTimeout()).isEqualTo(Duration.ofSeconds(8));
    assertThat(authCc.getUseStartTLS()).isTrue();
    assertThat(authCc.getConnectionStrategy().getClass()).isEqualTo(RoundRobinConnectionStrategy.class);

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
  @Parameters({"ldapBindDn", "ldapTestHost"})
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

    assertThat(cf.getTransport().getClass()).isNotNull();
    assertThat(cc.getLdapUrl()).isEqualTo(host);
    assertThat(ci.getBindDn()).isEqualTo(bindDn);
    assertThat(cc.getConnectTimeout()).isEqualTo(Duration.ofSeconds(8));
    assertThat(cc.getUseStartTLS()).isTrue();
    assertThat(cc.getConnectionStrategy().getClass()).isEqualTo(RoundRobinConnectionStrategy.class);

    assertThat(new Dn(searchRequest.getBaseDn()).format()).isEqualTo(new Dn(bindDn).subDn(1).format());
    assertThat(searchRequest.getSearchScope()).isEqualTo(SearchScope.OBJECT);
    assertThat(searchRequest.getTimeLimit()).isEqualTo(Duration.ofSeconds(5));
    assertThat(searchRequest.getBinaryAttributes()[0]).isEqualTo("jpegPhoto");

    final SearchDnResolver dnResolver = (SearchDnResolver) auth.getDnResolver();
    for (Function<LdapEntry, LdapEntry> handler : dnResolver.getEntryHandlers()) {
      if (handler instanceof MergeAttributeEntryHandler) {
        final MergeAttributeEntryHandler h = (MergeAttributeEntryHandler) handler;
        assertThat(h).isNotNull();
      } else if (handler instanceof DnAttributeEntryHandler) {
        final DnAttributeEntryHandler h = (DnAttributeEntryHandler) handler;
        assertThat(h.getDnAttributeName()).isEqualTo("myDN");
      } else {
        throw new Exception("Unknown search result handler type " + handler);
      }
    }

    final PooledConnectionFactory resolverCf = (PooledConnectionFactory) dnResolver.getConnectionFactory();
    assertThat(resolverCf.getMinPoolSize()).isEqualTo(1);
    assertThat(resolverCf.getMaxPoolSize()).isEqualTo(3);
    assertThat(resolverCf.isValidatePeriodically()).isTrue();
    assertThat(resolverCf.getValidator()).isNotNull();

    final IdlePruneStrategy pruneStrategy = (IdlePruneStrategy) resolverCf.getPruneStrategy();
    assertThat(pruneStrategy.getPrunePeriod()).isEqualTo(Duration.ofMinutes(1));
    assertThat(pruneStrategy.getIdleTime()).isEqualTo(Duration.ofMinutes(2));

    final ConnectionConfig authCc = resolverCf.getDefaultConnectionFactory().getConnectionConfig();
    final BindConnectionInitializer authCi = (BindConnectionInitializer) authCc.getConnectionInitializers()[0];
    assertThat(authCc.getLdapUrl()).isEqualTo(host);
    assertThat(authCi.getBindDn()).isEqualTo(bindDn);
    assertThat(authCc.getConnectTimeout()).isEqualTo(Duration.ofSeconds(8));
    assertThat(authCc.getUseStartTLS()).isTrue();
    assertThat(authCc.getConnectionStrategy().getClass()).isEqualTo(RoundRobinConnectionStrategy.class);

    assertThat(auth.getAuthenticationHandler().getClass())
      .isEqualTo(org.ldaptive.auth.CompareAuthenticationHandler.class);
    assertThat(auth.getDnResolver().getClass()).isEqualTo(org.ldaptive.auth.SearchDnResolver.class);

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

    assertThat(sr.getSearchScope()).isEqualTo(SearchScope.SUBTREE);
    assertThat(sr.getControls()).isNotNull();
  }
}
