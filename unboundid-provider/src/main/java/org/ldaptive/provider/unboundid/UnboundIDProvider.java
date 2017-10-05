/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.ssl.SSLSocketVerifier;
import com.unboundid.util.ssl.SSLUtil;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.Provider;
import org.ldaptive.provider.ProviderConnectionFactory;
import org.ldaptive.ssl.DefaultHostnameVerifier;
import org.ldaptive.ssl.HostnameVerifierAdapter;
import org.ldaptive.ssl.SslConfig;
import org.ldaptive.ssl.TLSSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    if (factory == null) {
      if (cc.getUseStartTLS() || cc.getUseSSL() || cc.getLdapUrl().toLowerCase().contains("ldaps://")) {
        if (cc.getSslConfig() != null && !cc.getSslConfig().isEmpty()) {
          final TLSSocketFactory sf = new TLSSocketFactory();
          sf.setSslConfig(SslConfig.newSslConfig(cc.getSslConfig()));
          try {
            sf.initialize();
          } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
          }
          factory = sf;
        } else if (cc.getUseSSL() || cc.getLdapUrl().toLowerCase().contains("ldaps://")) {
          final SSLUtil util = new SSLUtil();
          try {
            factory = util.createSSLSocketFactory();
          } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
          }
        }
      }
    }

    LDAPConnectionOptions options = config.getConnectionOptions();
    if (options == null) {
      options = getDefaultLDAPConnectionOptions(cc);
    }

    final ProviderConnectionFactory<UnboundIDProviderConfig> cf;
    if (cc.getUseStartTLS()) {
      cf = new UnboundIDStartTLSConnectionFactory(
        cc.getLdapUrl(),
        cc.getConnectionStrategy(),
        config,
        factory,
        options);
    } else {
      cf = new UnboundIDConnectionFactory(cc.getLdapUrl(), cc.getConnectionStrategy(), config, factory, options);
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
    if (cc.getConnectTimeout() != null) {
      options.setConnectTimeoutMillis((int) cc.getConnectTimeout().toMillis());
    }
    if (cc.getResponseTimeout() != null) {
      options.setResponseTimeoutMillis(cc.getResponseTimeout().toMillis());
    }
    // UnboundID does not do hostname verification by default
    // set a default hostname verifier if no SSL socket verifier configured
    final HostnameVerifier verifier;
    if (cc.getSslConfig() != null && !cc.getSslConfig().isEmpty()) {
      if (cc.getSslConfig().getHostnameVerifier() != null) {
        verifier = new HostnameVerifierAdapter(cc.getSslConfig().getHostnameVerifier());
      } else {
        verifier = new DefaultHostnameVerifier();
      }
    } else {
      verifier = new DefaultHostnameVerifier();
    }
    options.setSSLSocketVerifier(new SSLSocketVerifier()
    {
      /** Logger for this class. */
      private  final Logger logger = LoggerFactory.getLogger(getClass());


      @Override
      public void verifySSLSocket(
        final String host,
        final int port,
        final SSLSocket sslSocket)
        throws LDAPException
      {
        logger.trace("Verifying SSLSocket {} for host {} with verifier {}", sslSocket, host, verifier);
        final SSLSession session = sslSocket.getSession();
        try {
          // confirm that the trust manager succeeded
          session.getPeerCertificates();
          if (!verifier.verify(host, session)) {
            try {
              sslSocket.close();
            } catch (IOException e) {
              logger.debug("Error closing SSL socket", e);
            }
            throw new LDAPException(ResultCode.CONNECT_ERROR, "Hostname verification failed for " + host);
          }
        } catch (SSLPeerUnverifiedException e) {
          throw new LDAPException(ResultCode.CONNECT_ERROR, "Trust verification failed for " + host, e);
        }
      }
    });
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
