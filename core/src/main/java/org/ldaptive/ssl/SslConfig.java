/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.TrustManager;
import org.ldaptive.AbstractConfig;

/**
 * Contains all the configuration data for SSL and startTLS.
 *
 * @author  Middleware Services
 */
public class SslConfig extends AbstractConfig
{

  /** Configuration for the trust and authentication material to use for SSL and startTLS. */
  private CredentialConfig credentialConfig;

  /** Trust managers. */
  private TrustManager[] trustManagers;

  /** Certificate hostname verifier. */
  private CertificateHostnameVerifier hostnameVerifier;

  /** Enabled cipher suites. */
  private String[] enabledCipherSuites;

  /** Enabled protocol versions. */
  private String[] enabledProtocols;

  /** Handshake completed listeners. */
  private HandshakeCompletedListener[] handshakeCompletedListeners;

  /** Duration of time that handshakes will block. */
  private Duration handshakeTimeout = Duration.ofMinutes(1);


  /** Default constructor. */
  public SslConfig() {}


  /**
   * Creates a new ssl config.
   *
   * @param  config  credential config
   */
  public SslConfig(final CredentialConfig config)
  {
    credentialConfig = config;
  }


  /**
   * Creates a new ssl config.
   *
   * @param  managers  trust managers
   */
  public SslConfig(final TrustManager... managers)
  {
    trustManagers = managers;
  }


  /**
   * Creates a new ssl config.
   *
   * @param  config  credential config
   * @param  managers  trust managers
   */
  public SslConfig(final CredentialConfig config, final TrustManager... managers)
  {
    credentialConfig = config;
    trustManagers = managers;
  }


  /**
   * Returns whether this ssl config contains any configuration data.
   *
   * @return  whether all properties are null
   */
  public boolean isEmpty()
  {
    return
      credentialConfig == null && trustManagers == null && hostnameVerifier == null && enabledCipherSuites == null &&
        enabledProtocols == null && handshakeCompletedListeners == null;
  }


  /**
   * Returns the credential config.
   *
   * @return  credential config
   */
  public CredentialConfig getCredentialConfig()
  {
    return credentialConfig;
  }


  /**
   * Sets the credential config.
   *
   * @param  config  credential config
   */
  public void setCredentialConfig(final CredentialConfig config)
  {
    checkImmutable();
    logger.trace("setting credentialConfig: {}", config);
    credentialConfig = config;
  }


  /**
   * Returns the trust managers.
   *
   * @return  trust managers
   */
  public TrustManager[] getTrustManagers()
  {
    return trustManagers;
  }


  /**
   * Sets the trust managers.
   *
   * @param  managers  trust managers
   */
  public void setTrustManagers(final TrustManager... managers)
  {
    checkImmutable();
    logger.trace("setting trustManagers: {}", Arrays.toString(managers));
    trustManagers = managers;
  }


  /**
   * Returns the hostname verifier.
   *
   * @return  hostname verifier
   */
  public CertificateHostnameVerifier getHostnameVerifier()
  {
    return hostnameVerifier;
  }


  /**
   * Sets the hostname verifier.
   *
   * @param  verifier  hostname verifier
   */
  public void setHostnameVerifier(final CertificateHostnameVerifier verifier)
  {
    checkImmutable();
    logger.trace("setting hostnameVerifier: {}", verifier);
    hostnameVerifier = verifier;
  }


  /**
   * Returns the names of the SSL cipher suites to use for secure connections.
   *
   * @return  cipher suites
   */
  public String[] getEnabledCipherSuites()
  {
    return enabledCipherSuites;
  }


  /**
   * Sets the SSL cipher suites to use for secure connections.
   *
   * @param  suites  cipher suites
   */
  public void setEnabledCipherSuites(final String... suites)
  {
    checkImmutable();
    logger.trace("setting enabledCipherSuites: {}", Arrays.toString(suites));
    enabledCipherSuites = suites;
  }


  /**
   * Returns the names of the SSL protocols to use for secure connections.
   *
   * @return  enabled protocols
   */
  public String[] getEnabledProtocols()
  {
    return enabledProtocols;
  }


  /**
   * Sets the SSL protocol versions to use for secure connections.
   *
   * @param  protocols  enabled protocols
   */
  public void setEnabledProtocols(final String... protocols)
  {
    checkImmutable();
    logger.trace("setting enabledProtocols: {}", Arrays.toString(protocols));
    enabledProtocols = protocols;
  }


  /**
   * Returns the handshake completed listeners to use for secure connections.
   *
   * @return  handshake completed listeners
   */
  public HandshakeCompletedListener[] getHandshakeCompletedListeners()
  {
    return handshakeCompletedListeners;
  }


  /**
   * Sets the handshake completed listeners to use for secure connections.
   *
   * @param  listeners  for SSL handshake events
   */
  public void setHandshakeCompletedListeners(final HandshakeCompletedListener... listeners)
  {
    checkImmutable();
    logger.trace("setting handshakeCompletedListeners: {}", Arrays.toString(handshakeCompletedListeners));
    handshakeCompletedListeners = listeners;
  }


  /**
   * Returns the handshake timeout.
   *
   * @return  timeout
   */
  public Duration getHandshakeTimeout()
  {
    return handshakeTimeout;
  }


  /**
   * Sets the maximum amount of time that handshakes will block.
   *
   * @param  time  timeout for handshakes
   */
  public void setHandshakeTimeout(final Duration time)
  {
    checkImmutable();
    if (time != null && time.isNegative()) {
      throw new IllegalArgumentException("Handshake timeout cannot be negative");
    }
    logger.trace("setting handshakeTimeout: {}", time);
    handshakeTimeout = time;
  }


  /**
   * Returns a ssl config initialized with the supplied config.
   *
   * @param  config  ssl config to read properties from
   *
   * @return  ssl config
   */
  public static SslConfig copy(final SslConfig config)
  {
    final SslConfig sc = new SslConfig();
    sc.setCredentialConfig(config.getCredentialConfig());
    sc.setTrustManagers(config.getTrustManagers());
    sc.setHostnameVerifier(config.getHostnameVerifier());
    sc.setEnabledCipherSuites(config.getEnabledCipherSuites());
    sc.setEnabledProtocols(config.getEnabledProtocols());
    sc.setHandshakeCompletedListeners(config.getHandshakeCompletedListeners());
    sc.setHandshakeTimeout(config.getHandshakeTimeout());
    return sc;
  }


  /**
   * Creates an {@link SSLContextInitializer} from this configuration. If a {@link CredentialConfig} is provided it is
   * used, otherwise a {@link DefaultSSLContextInitializer} is created.
   *
   * @return  SSL context initializer
   *
   * @throws  GeneralSecurityException  if the SSL context initializer cannot be created
   */
  public SSLContextInitializer createSSLContextInitializer()
    throws GeneralSecurityException
  {
    final SSLContextInitializer initializer;
    if (credentialConfig != null) {
      initializer = credentialConfig.createSSLContextInitializer();
    } else {
      if (trustManagers != null) {
        initializer = new DefaultSSLContextInitializer(false);
      } else {
        initializer = new DefaultSSLContextInitializer(true);
      }
    }

    if (trustManagers != null) {
      initializer.setTrustManagers(trustManagers);
    }
    return initializer;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("credentialConfig=").append(credentialConfig).append(", ")
      .append("trustManagers=").append(Arrays.toString(trustManagers)).append(", ")
      .append("hostnameVerifier=").append(hostnameVerifier).append(", ")
      .append("enabledCipherSuites=").append(Arrays.toString(enabledCipherSuites)).append(", ")
      .append("enabledProtocols=").append(Arrays.toString(enabledProtocols)).append(", ")
      .append("handshakeCompletedListeners=").append(Arrays.toString(handshakeCompletedListeners)).append(", ")
      .append("handshakeTimeout=").append(handshakeTimeout)
      .append("]").toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final SslConfig object = new SslConfig();


    protected Builder() {}


    public Builder credentialConfig(final CredentialConfig config)
    {
      object.setCredentialConfig(config);
      return this;
    }


    public Builder trustManagers(final TrustManager... managers)
    {
      object.setTrustManagers(managers);
      return this;
    }


    public Builder hostnameVerifier(final CertificateHostnameVerifier verifier)
    {
      object.setHostnameVerifier(verifier);
      return this;
    }


    public Builder cipherSuites(final String... suites)
    {
      object.setEnabledCipherSuites(suites);
      return this;
    }


    public Builder protocols(final String... protocols)
    {
      object.setEnabledProtocols(protocols);
      return this;
    }


    public Builder handshakeListeners(final HandshakeCompletedListener... listeners)
    {
      object.setHandshakeCompletedListeners(listeners);
      return this;
    }


    public Builder handshakeTimeout(final Duration timeout)
    {
      object.setHandshakeTimeout(timeout);
      return this;
    }


    public SslConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
