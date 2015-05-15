/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import javax.net.ssl.SSLSocketFactory;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.ssl.TLSSocketFactory;

/**
 * UnboundID provider implementation. Provides connection factories for clear, SSL, and TLS connections.
 *
 * @author  Middleware Services
 */
public class UnboundIDProvider implements Provider<UnboundIDProviderConfig>
{

  /** Provider configuration. */
  private UnboundIDProviderConfig config = new UnboundIDProviderConfig();


  @Override
  public ProviderConnectionFactory<UnboundIDProviderConfig> getConnectionFactory(final ConnectionConfig cc)
  {
    SSLSocketFactory factory = config.getSSLSocketFactory();
    // UnboundID does not do hostname verification by default
    // set a default hostname verifier if no trust settings have been configured
    if (factory == null && (cc.getUseStartTLS() || cc.getUseSSL())) {
      factory = getHostnameVerifierSocketFactory(cc);
    }

    LDAPConnectionOptions options = config.getConnectionOptions();
    if (options == null) {
      options = getDefaultLDAPConnectionOptions(cc);
    }

    ProviderConnectionFactory<UnboundIDProviderConfig> cf;
    if (cc.getUseStartTLS()) {
      cf = new UnboundIDStartTLSConnectionFactory(cc.getLdapUrl(), config, factory, options);
    } else if (cc.getUseSSL()) {
      cf = new UnboundIDConnectionFactory(cc.getLdapUrl(), config, factory, options);
    } else {
      cf = new UnboundIDConnectionFactory(cc.getLdapUrl(), config, null, options);
    }
    return cf;
  }


  /**
   * Returns an SSL socket factory configured with a default hostname verifier.
   *
   * @param  cc  connection configuration
   *
   * @return  SSL socket factory
   */
  protected SSLSocketFactory getHostnameVerifierSocketFactory(final ConnectionConfig cc)
  {
    final LdapURL ldapUrl = new LdapURL(cc.getLdapUrl());
    return TLSSocketFactory.getHostnameVerifierFactory(cc.getSslConfig(), ldapUrl.getHostnames());
  }


  /**
   * Returns the default connection options for this provider.
   *
   * @param  cc  to configure options with
   *
   * @return  ldap connection options
   */
  protected LDAPConnectionOptions getDefaultLDAPConnectionOptions(final ConnectionConfig cc)
  {
    final LDAPConnectionOptions options = new LDAPConnectionOptions();
    options.setConnectTimeoutMillis(cc.getConnectTimeout() > 0 ? (int) cc.getConnectTimeout() : 0);
    options.setResponseTimeoutMillis(cc.getResponseTimeout());
    return options;
  }


  @Override
  public UnboundIDProviderConfig getProviderConfig()
  {
    return config;
  }


  @Override
  public void setProviderConfig(final UnboundIDProviderConfig pc)
  {
    config = pc;
  }


  @Override
  public UnboundIDProvider newInstance()
  {
    return new UnboundIDProvider();
  }
}
