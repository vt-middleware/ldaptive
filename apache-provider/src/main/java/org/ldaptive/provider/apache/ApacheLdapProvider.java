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
import org.ldaptive.ssl.CertificateHostnameVerifier;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.DefaultSSLContextInitializer;
import org.ldaptive.ssl.HostnameVerifierConfig;
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


  @Override
  public ProviderConnectionFactory<ApacheLdapProviderConfig> getConnectionFactory(final ConnectionConfig cc)
  {
    LdapConnectionConfig lcc = config.getLdapConnectionConfig();
    if (lcc == null) {
      lcc = getDefaultLdapConnectionConfig(cc);
    }
    return new ApacheLdapConnectionFactory(
      cc.getLdapUrl(),
      cc.getConnectionStrategy(),
      config,
      lcc,
      cc.getUseStartTLS(),
      cc.getResponseTimeout());
  }


  /**
   * Returns an SSLContextInitializer configured with a hostname verifier. Uses a {@link DefaultHostnameVerifier} if no
   * SSL config has been configured.
   *
   * @param  cc  connection configuration
   *
   * @return  SSL Context Initializer
   */
  protected SSLContextInitializer getHostnameVerifierSSLContextInitializer(final ConnectionConfig cc)
  {
    final LdapURL ldapUrl = new LdapURL(cc.getLdapUrl());
    final SSLContextInitializer contextInit;
    if (cc.getSslConfig() != null && !cc.getSslConfig().isEmpty()) {
      final CredentialConfig credConfig = cc.getSslConfig().getCredentialConfig();
      final TrustManager[] managers = cc.getSslConfig().getTrustManagers();
      final CertificateHostnameVerifier verifier = cc.getSslConfig().getHostnameVerifier();
      if (credConfig != null) {
        try {
          contextInit = credConfig.createSSLContextInitializer();
        } catch (GeneralSecurityException e) {
          throw new IllegalArgumentException(e);
        }
      } else {
        if (managers != null) {
          contextInit = new DefaultSSLContextInitializer(false);
        } else {
          contextInit = new DefaultSSLContextInitializer(true);
        }
      }

      if (managers != null) {
        contextInit.setTrustManagers(managers);
      }
      if (verifier != null) {
        contextInit.setHostnameVerifierConfig(new HostnameVerifierConfig(verifier, ldapUrl.getHostnames()));
      } else {
        contextInit.setHostnameVerifierConfig(
          new HostnameVerifierConfig(new DefaultHostnameVerifier(), ldapUrl.getHostnames()));
      }
    } else {
      contextInit = new DefaultSSLContextInitializer(true);
      contextInit.setTrustManagers(
        new HostnameVerifyingTrustManager(new DefaultHostnameVerifier(), ldapUrl.getHostnames()));
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
  protected LdapConnectionConfig getDefaultLdapConnectionConfig(final ConnectionConfig cc)
  {
    final LdapConnectionConfig lcc = new LdapConnectionConfig();
    if (cc.getUseStartTLS() || cc.getUseSSL() || cc.getLdapUrl().toLowerCase().contains("ldaps://")) {
      final SSLContextInitializer contextInit = getHostnameVerifierSSLContextInitializer(cc);
      final TrustManager[] trustManagers;
      final KeyManager[] keyManagers;
      try {
        trustManagers = contextInit.getTrustManagers();
        keyManagers = contextInit.getKeyManagers();
      } catch (GeneralSecurityException e) {
        throw new IllegalArgumentException(e);
      }

      lcc.setUseSsl(cc.getUseSSL() || cc.getLdapUrl().toLowerCase().contains("ldaps://"));
      lcc.setTrustManagers(trustManagers);
      lcc.setKeyManagers(keyManagers);
      if (cc.getSslConfig() != null && cc.getSslConfig().getEnabledCipherSuites() != null) {
        lcc.setEnabledCipherSuites(cc.getSslConfig().getEnabledCipherSuites());
      }
      if (cc.getSslConfig() != null && cc.getSslConfig().getEnabledProtocols() != null) {
        lcc.setSslProtocol(cc.getSslConfig().getEnabledProtocols()[0]);
      }
    }
    return lcc;
  }


  @Override
  public ApacheLdapProviderConfig getProviderConfig()
  {
    return config;
  }


  @Override
  public void setProviderConfig(final ApacheLdapProviderConfig pc)
  {
    config = pc;
  }


  @Override
  public ApacheLdapProvider newInstance()
  {
    return new ApacheLdapProvider();
  }
}
