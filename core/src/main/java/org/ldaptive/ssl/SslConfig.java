/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.TrustManager;
import org.ldaptive.AbstractConfig;
import org.ldaptive.LdapUtils;

/**
 * Contains all the configuration data for SSL and startTLS.
 *
 * @author  Middleware Services
 */
public final class SslConfig extends AbstractConfig
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
    trustManagers = LdapUtils.copyArray(managers);
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
    trustManagers = LdapUtils.copyArray(managers);
  }


  @Override
  public void freeze()
  {
    super.freeze();
    freeze(credentialConfig);
    freeze(trustManagers);
    freeze(hostnameVerifier);
    freeze(handshakeCompletedListeners);
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
    assertMutable();
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
    return LdapUtils.copyArray(trustManagers);
  }


  /**
   * Sets the trust managers.
   *
   * @param  managers  trust managers
   */
  public void setTrustManagers(final TrustManager... managers)
  {
    assertMutable();
    checkArrayContainsNull(managers);
    logger.trace("setting trustManagers: {}", Arrays.toString(managers));
    trustManagers = LdapUtils.copyArray(managers);
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
    assertMutable();
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
    return LdapUtils.copyArray(enabledCipherSuites);
  }


  /**
   * Sets the SSL cipher suites to use for secure connections.
   *
   * @param  suites  cipher suites
   */
  public void setEnabledCipherSuites(final String... suites)
  {
    assertMutable();
    checkArrayContainsNull(suites);
    logger.trace("setting enabledCipherSuites: {}", Arrays.toString(suites));
    enabledCipherSuites = LdapUtils.copyArray(suites);
  }


  /**
   * Returns the names of the SSL protocols to use for secure connections.
   *
   * @return  enabled protocols
   */
  public String[] getEnabledProtocols()
  {
    return LdapUtils.copyArray(enabledProtocols);
  }


  /**
   * Sets the SSL protocol versions to use for secure connections.
   *
   * @param  protocols  enabled protocols
   */
  public void setEnabledProtocols(final String... protocols)
  {
    assertMutable();
    checkArrayContainsNull(protocols);
    logger.trace("setting enabledProtocols: {}", Arrays.toString(protocols));
    enabledProtocols = LdapUtils.copyArray(protocols);
  }


  /**
   * Returns the handshake completed listeners to use for secure connections.
   *
   * @return  handshake completed listeners
   */
  public HandshakeCompletedListener[] getHandshakeCompletedListeners()
  {
    return LdapUtils.copyArray(handshakeCompletedListeners);
  }


  /**
   * Sets the handshake completed listeners to use for secure connections.
   *
   * @param  listeners  for SSL handshake events
   */
  public void setHandshakeCompletedListeners(final HandshakeCompletedListener... listeners)
  {
    assertMutable();
    checkArrayContainsNull(listeners);
    logger.trace("setting handshakeCompletedListeners: {}", Arrays.toString(handshakeCompletedListeners));
    handshakeCompletedListeners = LdapUtils.copyArray(listeners);
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
    assertMutable();
    LdapUtils.assertNotNullArgOr(time, Duration::isNegative, "Handshake timeout cannot be null or negative");
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
    final SslConfig copy = new SslConfig();
    copy.setCredentialConfig(config.credentialConfig);
    copy.setTrustManagers(config.trustManagers);
    copy.setHostnameVerifier(config.hostnameVerifier);
    copy.setEnabledCipherSuites(config.enabledCipherSuites);
    copy.setEnabledProtocols(config.enabledProtocols);
    copy.setHandshakeCompletedListeners(config.handshakeCompletedListeners);
    copy.setHandshakeTimeout(config.handshakeTimeout);
    return copy;
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
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "credentialConfig=" + credentialConfig + ", " +
      "trustManagers=" + Arrays.toString(trustManagers) + ", " +
      "hostnameVerifier=" + hostnameVerifier + ", " +
      "enabledCipherSuites=" + Arrays.toString(enabledCipherSuites) + ", " +
      "enabledProtocols=" + Arrays.toString(enabledProtocols) + ", " +
      "handshakeCompletedListeners=" + Arrays.toString(handshakeCompletedListeners) + ", " +
      "handshakeTimeout=" + handshakeTimeout + "]";
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
  public static final class Builder
  {


    private final SslConfig object = new SslConfig();


    private Builder() {}


    public Builder freeze()
    {
      object.freeze();
      return this;
    }


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
