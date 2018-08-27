/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * TLSSocketFactory implementation that uses a thread local variable to store configuration. Useful for SSL
 * configurations that can only retrieve the SSLSocketFactory from getDefault().
 *
 * @author  Middleware Services
 */
public class ThreadLocalTLSSocketFactory extends TLSSocketFactory
{

  /** Thread local instance of the ssl config. */
  private static final ThreadLocalSslConfig THREAD_LOCAL_SSL_CONFIG = new ThreadLocalSslConfig();


  @Override
  public SslConfig getSslConfig()
  {
    return THREAD_LOCAL_SSL_CONFIG.get();
  }


  @Override
  public void setSslConfig(final SslConfig config)
  {
    THREAD_LOCAL_SSL_CONFIG.set(config);
  }


  /**
   * Removes the ssl config from the current thread-local value.
   */
  public void removeSslConfig()
  {
    THREAD_LOCAL_SSL_CONFIG.remove();
  }


  /**
   * This returns the default SSL socket factory.
   *
   * @return  socket factory
   */
  public static SocketFactory getDefault()
  {
    final ThreadLocalTLSSocketFactory sf = new ThreadLocalTLSSocketFactory();
    if (sf.getSslConfig() == null) {
      throw new NullPointerException("Thread local SslConfig has not been set");
    }
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
  public static SSLSocketFactory getHostnameVerifierFactory(final SslConfig config, final String[] names)
  {
    final ThreadLocalTLSSocketFactory sf = new ThreadLocalTLSSocketFactory();
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


  /** Thread local class for {@link SslConfig}. */
  private static class ThreadLocalSslConfig extends ThreadLocal<SslConfig> {}
}
