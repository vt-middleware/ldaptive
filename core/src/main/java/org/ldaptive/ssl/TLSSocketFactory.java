/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * An extension of SSLSocketFactory that leverages an SSL context initializer. Note that {@link #initialize()} must be
 * called prior to using this socket factory. This means that this class cannot be passed to implementations that expect
 * the socket factory to function immediately after construction.
 *
 * @author  Middleware Services
 */
public class TLSSocketFactory extends AbstractTLSSocketFactory
{


  /**
   * Creates the underlying SSLContext using truststore and keystore attributes and makes this factory ready for use.
   * Must be called before factory can be used.
   *
   * @throws  GeneralSecurityException  if the SSLContext cannot be created
   */
  @Override
  public void initialize()
    throws GeneralSecurityException
  {
    final SSLContextInitializer contextInitializer = createSSLContextInitializer();
    logger.trace("Using SSLContextInitializer={}", contextInitializer);
    final SSLContext ctx = contextInitializer.initSSLContext(DEFAULT_PROTOCOL);
    factory = ctx.getSocketFactory();
  }


  /**
   * Creates a {@link SSLContextInitializer} for use with this socket factory.
   *
   * @return  SSL context initializer
   *
   * @throws GeneralSecurityException  if the SSL context initializer cannot be created
   */
  protected SSLContextInitializer createSSLContextInitializer()
    throws GeneralSecurityException
  {
    final SSLContextInitializer contextInitializer;
    final SslConfig sslConfig = getSslConfig();
    if (sslConfig != null) {
      final CredentialConfig credConfig = sslConfig.getCredentialConfig();
      final TrustManager[] managers = sslConfig.getTrustManagers();
      final HostnameVerifierConfig verifierConfig = sslConfig.getHostnameVerifierConfig();
      if (credConfig != null) {
        contextInitializer = credConfig.createSSLContextInitializer();
      } else {
        if (managers != null) {
          contextInitializer = new DefaultSSLContextInitializer(false);
        } else {
          contextInitializer = new DefaultSSLContextInitializer(true);
        }
      }

      if (managers != null) {
        contextInitializer.setTrustManagers(managers);
      }
      if (verifierConfig != null) {
        contextInitializer.setHostnameVerifierConfig(verifierConfig);
      }
    } else {
      contextInitializer = new DefaultSSLContextInitializer();
    }
    return contextInitializer;
  }


  /**
   * Returns the default SSL socket factory.
   *
   * @return  socket factory
   */
  public static SocketFactory getDefault()
  {
    final TLSSocketFactory sf = new TLSSocketFactory();
    try {
      sf.initialize();
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException("Error initializing socket factory", e);
    }
    return sf;
  }


  /**
   * Returns an instance of this socket factory configured with a hostname verifying trust manager. If the supplied ssl
   * config does not contain trust managers, {@link HostnameVerifyingTrustManager} with {@link DefaultHostnameVerifier}
   * is set. See {@link #addHostnameVerifyingTrustManager(SslConfig, String[])}.
   *
   * @param  config  to set on the socket factory
   * @param  names  to use for hostname verification
   *
   * @return  socket factory
   */
  @SuppressWarnings("RedundantArrayCreation")
  public static SSLSocketFactory getHostnameVerifierFactory(final SslConfig config, final String[] names)
  {
    final TLSSocketFactory sf = new TLSSocketFactory();
    if (config != null && !config.isEmpty()) {
      sf.setSslConfig(SslConfig.newSslConfig(config));
    } else {
      sf.setSslConfig(new SslConfig());
    }
    final CertificateHostnameVerifier verifier = sf.getSslConfig().getHostnameVerifier();
    if (verifier == null) {
      sf.getSslConfig().setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), names));
    } else {
      sf.getSslConfig().setHostnameVerifierConfig(new HostnameVerifierConfig(verifier, names));
    }
    try {
      sf.initialize();
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
    return sf;
  }


  /**
   * Adds a {@link HostnameVerifyingTrustManager} to the supplied config if no trust managers have been configured. A
   * {@link DefaultTrustManager} is also added in no {@link CredentialConfig} has been configured.
   *
   * @deprecated  {@link HostnameVerifierConfig} should be used for hostname verification
   *
   * @param  config  to modify
   * @param  names  of the hosts to verify
   */
  @Deprecated
  protected static void addHostnameVerifyingTrustManager(final SslConfig config, final String[] names)
  {
    if (config.getTrustManagers() == null) {
      if (config.getCredentialConfig() == null) {
        config.setTrustManagers(
          new DefaultTrustManager(),
          new HostnameVerifyingTrustManager(new DefaultHostnameVerifier(), names));
      } else {
        config.setTrustManagers(new HostnameVerifyingTrustManager(new DefaultHostnameVerifier(), names));
      }
    }
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, sslConfig=%s]",
        getClass().getName(),
        hashCode(),
        getFactory(),
        getSslConfig());
  }
}
