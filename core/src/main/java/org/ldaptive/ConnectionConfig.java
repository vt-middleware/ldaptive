/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.ldaptive.ssl.SslConfig;

/**
 * Contains all the configuration data needed to control connections.
 *
 * @author  Middleware Services
 */
public class ConnectionConfig extends AbstractConfig
{

  /** URL to the LDAP(s). */
  private String ldapUrl;

  /** Duration of time that connects will block. */
  private Duration connectTimeout;

  /** Duration of time to wait for responses. */
  private Duration responseTimeout;

  /** Configuration for SSL and startTLS connections. */
  private SslConfig sslConfig;

  /** Connect to LDAP using SSL protocol. */
  private boolean useSSL;

  /** Connect to LDAP using startTLS. */
  private boolean useStartTLS;

  /** Connection initializer to execute on {@link Connection#open()}. */
  private ConnectionInitializer connectionInitializer;

  /** Connection strategy. */
  private ConnectionStrategy connectionStrategy = new ActivePassiveConnectionStrategy();


  /** Default constructor. */
  public ConnectionConfig() {}


  /**
   * Creates a new connection config.
   *
   * @param  url  to connect to
   */
  public ConnectionConfig(final String url)
  {
    setLdapUrl(url);
  }


  /**
   * Returns the ldap url.
   *
   * @return  ldap url
   */
  public String getLdapUrl()
  {
    return ldapUrl;
  }


  /**
   * Sets the ldap url.
   *
   * @param  url  of the ldap
   */
  public void setLdapUrl(final String url)
  {
    checkImmutable();
    checkStringInput(url, true);
    logger.trace("setting ldapUrl: {}", url);
    ldapUrl = url;
  }


  /**
   * Returns the connect timeout. If this value is null, then the provider default will be used.
   *
   * @return  timeout
   */
  public Duration getConnectTimeout()
  {
    return connectTimeout;
  }


  /**
   * Sets the maximum amount of time that connects will block.
   *
   * @param  time  timeout for connects
   */
  public void setConnectTimeout(final Duration time)
  {
    checkImmutable();
    if (time != null && time.isNegative()) {
      throw new IllegalArgumentException("Connect timeout cannot be negative");
    }
    logger.trace("setting connectTimeout: {}", time);
    connectTimeout = time;
  }


  /**
   * Sets the maximum amount of time that connects will block.
   *
   * @param  time  in duration syntax
   */
  public void setConnectTimeoutDuration(final String time)
  {
    checkImmutable();
    setConnectTimeout(Duration.parse(time));
  }


  /**
   * Returns the response timeout. If this value is null, then the provider default will be used.
   *
   * @return  timeout
   */
  public Duration getResponseTimeout()
  {
    return responseTimeout;
  }


  /**
   * Sets the maximum amount of time that operations will wait for a response.
   *
   * @param  time  timeout for responses
   */
  public void setResponseTimeout(final Duration time)
  {
    checkImmutable();
    if (time != null && time.isNegative()) {
      throw new IllegalArgumentException("Connect timeout cannot be negative");
    }
    logger.trace("setting responseTimeout: {}", time);
    responseTimeout = time;
  }


  /**
   * Sets the maximum amount of time that operations will wait for a response.
   *
   * @param  time  in duration syntax
   */
  public void setResponseTimeoutDuration(final String time)
  {
    checkImmutable();
    setResponseTimeout(Duration.parse(time));
  }


  /**
   * Returns the ssl config.
   *
   * @return  ssl config
   */
  public SslConfig getSslConfig()
  {
    return sslConfig;
  }


  /**
   * Sets the ssl config.
   *
   * @param  config  ssl config
   */
  public void setSslConfig(final SslConfig config)
  {
    checkImmutable();
    logger.trace("setting sslConfig: {}", config);
    sslConfig = config;
  }


  /**
   * Returns whether the SSL protocol will be used for connections.
   *
   * @return  whether the SSL protocol will be used
   */
  public boolean getUseSSL()
  {
    return useSSL;
  }


  /**
   * Sets whether the SSL protocol will be used for connections.
   *
   * @param  b  whether the SSL protocol will be used
   */
  public void setUseSSL(final boolean b)
  {
    checkImmutable();
    logger.trace("setting useSSL: {}", b);
    useSSL = b;
  }


  /**
   * Returns whether startTLS will be used for connections.
   *
   * @return  whether startTLS will be used
   */
  public boolean getUseStartTLS()
  {
    return useStartTLS;
  }


  /**
   * Sets whether startTLS will be used for connections.
   *
   * @param  b  whether startTLS will be used
   */
  public void setUseStartTLS(final boolean b)
  {
    checkImmutable();
    logger.trace("setting useStartTLS: {}", b);
    useStartTLS = b;
  }


  /**
   * Returns the connection initializer.
   *
   * @return  connection initializer
   */
  public ConnectionInitializer getConnectionInitializer()
  {
    return connectionInitializer;
  }


  /**
   * Sets the connection initializer.
   *
   * @param  initializer  connection initializer
   */
  public void setConnectionInitializer(final ConnectionInitializer initializer)
  {
    checkImmutable();
    logger.trace("setting connectionInitializer: {}", initializer);
    connectionInitializer = initializer;
  }


  /**
   * Returns the connection strategy.
   *
   * @return  strategy for making connections
   */
  public ConnectionStrategy getConnectionStrategy()
  {
    return connectionStrategy;
  }


  /**
   * Sets the connection strategy.
   *
   * @param  strategy  for making connections
   */
  public void setConnectionStrategy(final ConnectionStrategy strategy)
  {
    checkImmutable();
    logger.trace("setting connectionStrategy: {}", strategy);
    connectionStrategy = strategy;
  }


  /**
   * Returns a connection config initialized with the supplied config.
   *
   * @param  config  connection config to read properties from
   *
   * @return  connection config
   */
  public static ConnectionConfig newConnectionConfig(final ConnectionConfig config)
  {
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl(config.getLdapUrl());
    cc.setConnectTimeout(config.getConnectTimeout());
    cc.setResponseTimeout(config.getResponseTimeout());
    cc.setSslConfig(config.getSslConfig());
    cc.setUseSSL(config.getUseSSL());
    cc.setUseStartTLS(config.getUseStartTLS());
    cc.setConnectionInitializer(config.getConnectionInitializer());
    cc.setConnectionStrategy(config.getConnectionStrategy());
    return cc;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::ldapUrl=%s, connectTimeout=%s, responseTimeout=%s, " +
        "sslConfig=%s, useSSL=%s, useStartTLS=%s, connectionInitializer=%s, connectionStrategy=%s]",
        getClass().getName(),
        hashCode(),
        ldapUrl,
        connectTimeout,
        responseTimeout,
        sslConfig,
        useSSL,
        useStartTLS,
        connectionInitializer,
        connectionStrategy);
  }
}
