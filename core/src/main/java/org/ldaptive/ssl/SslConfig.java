/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.util.Arrays;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.TrustManager;
import org.ldaptive.AbstractConfig;

/**
 * Contains all the configuration data for SSL and startTLS. Providers are not guaranteed to support all the options
 * contained here.
 *
 * @author  Middleware Services
 */
public class SslConfig extends AbstractConfig
{

  /** Configuration for the trust and authentication material to use for SSL and startTLS. */
  private CredentialConfig credentialConfig;

  /** Trust managers. */
  private TrustManager[] trustManagers;

  /** Enabled cipher suites. */
  private String[] enabledCipherSuites;

  /** Enabled protocol versions. */
  private String[] enabledProtocols;

  /** Handshake completed listeners. */
  private HandshakeCompletedListener[] handshakeCompletedListeners;


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
      credentialConfig == null && trustManagers == null && enabledCipherSuites == null && enabledProtocols == null &&
      handshakeCompletedListeners == null;
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
   * Returns a ssl config initialized with the supplied config.
   *
   * @param  config  ssl config to read properties from
   *
   * @return  ssl config
   */
  public static SslConfig newSslConfig(final SslConfig config)
  {
    final SslConfig sc = new SslConfig();
    sc.setCredentialConfig(config.getCredentialConfig());
    sc.setTrustManagers(config.getTrustManagers());
    sc.setEnabledCipherSuites(config.getEnabledCipherSuites());
    sc.setEnabledProtocols(config.getEnabledProtocols());
    sc.setHandshakeCompletedListeners(config.getHandshakeCompletedListeners());
    return sc;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::credentialConfig=%s, trustManagers=%s, " +
        "enabledCipherSuites=%s, enabledProtocols=%s, " +
        "handshakeCompletedListeners=%s]",
        getClass().getName(),
        hashCode(),
        credentialConfig,
        Arrays.toString(trustManagers),
        Arrays.toString(enabledCipherSuites),
        Arrays.toString(enabledProtocols),
        Arrays.toString(handshakeCompletedListeners));
  }
}
