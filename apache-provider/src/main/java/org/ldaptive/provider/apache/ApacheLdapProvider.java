/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.apache;

import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.DefaultSSLContextInitializer;
import org.ldaptive.ssl.HostnameVerifyingTrustManager;
import org.ldaptive.ssl.SSLContextInitializer;

/**
 * Exposes a connection factory for creating ldap connections with Apache LDAP.
 *
 * @author  Middleware Services
 */
public class ApacheLdapProvider implements Provider<ApacheLdapProviderConfig>
{


  /** Provider configuration. */
  private ApacheLdapProviderConfig config = new ApacheLdapProviderConfig();


  /** {@inheritDoc} */
  @Override
  public ProviderConnectionFactory<ApacheLdapProviderConfig>
  getConnectionFactory(final ConnectionConfig cc)
  {
    LdapConnectionConfig lcc = config.getLdapConnectionConfig();
    if (lcc == null) {
      lcc = getDefaultLdapConnectionConfig(cc);
    }
    return
      new ApacheLdapConnectionFactory(
        cc.getLdapUrl(),
        config,
        lcc,
        cc.getUseStartTLS(),
        cc.getResponseTimeout());
  }


  /**
   * Returns an SSLContextInitializer configured with a default hostname
   * verifier. Uses a {@link DefaultHostnameVerifier} if no credential config
   * has been configured.
   *
   * @param  cc  connection configuration
   *
   * @return  SSL Context Initializer
   */
  protected SSLContextInitializer getHostnameVerifierSSLContextInitializer(
    final ConnectionConfig cc)
  {
    SSLContextInitializer contextInit;
    if (cc.getSslConfig() != null &&
        cc.getSslConfig().getCredentialConfig() != null) {
      try {
        final CredentialConfig credConfig =
          cc.getSslConfig().getCredentialConfig();
        contextInit = credConfig.createSSLContextInitializer();
      } catch (GeneralSecurityException e) {
        throw new IllegalArgumentException(e);
      }
    } else {
      contextInit = new DefaultSSLContextInitializer();
    }
    if (cc.getSslConfig() != null &&
        cc.getSslConfig().getTrustManagers() != null) {
      contextInit.setTrustManagers(cc.getSslConfig().getTrustManagers());
    } else {
      final LdapURL ldapUrl = new LdapURL(cc.getLdapUrl());
      contextInit.setTrustManagers(
        new HostnameVerifyingTrustManager(
          new DefaultHostnameVerifier(),
          ldapUrl.getEntriesAsString()));
    }
    return contextInit;
  }


  /**
   * Returns the default connection configuration for this provider.
   *
   * @param  cc  to configure with
   *
   * @return  ldap connection configuration
   */
  protected LdapConnectionConfig getDefaultLdapConnectionConfig(
    final ConnectionConfig cc)
  {
    final LdapConnectionConfig lcc = new LdapConnectionConfig();
    if (cc.getUseSSL() || cc.getUseStartTLS()) {
      final SSLContextInitializer contextInit =
        getHostnameVerifierSSLContextInitializer(cc);
      TrustManager[] trustManagers;
      KeyManager[] keyManagers;
      try {
        trustManagers = contextInit.getTrustManagers();
        keyManagers = contextInit.getKeyManagers();
      } catch (GeneralSecurityException e) {
        throw new IllegalArgumentException(e);
      }

      lcc.setUseSsl(cc.getUseSSL());
      lcc.setTrustManagers(trustManagers);
      lcc.setKeyManagers(keyManagers);
      if (cc.getSslConfig() != null &&
          cc.getSslConfig().getEnabledCipherSuites() != null) {
        lcc.setEnabledCipherSuites(cc.getSslConfig().getEnabledCipherSuites());
      }
      if (cc.getSslConfig() != null &&
          cc.getSslConfig().getEnabledProtocols() != null) {
        lcc.setSslProtocol(cc.getSslConfig().getEnabledProtocols()[0]);
      }
    }
    return lcc;
  }


  /** {@inheritDoc} */
  @Override
  public ApacheLdapProviderConfig getProviderConfig()
  {
    return config;
  }


  /** {@inheritDoc} */
  @Override
  public void setProviderConfig(final ApacheLdapProviderConfig pc)
  {
    config = pc;
  }


  /** {@inheritDoc} */
  @Override
  public ApacheLdapProvider newInstance()
  {
    return new ApacheLdapProvider();
  }
}
