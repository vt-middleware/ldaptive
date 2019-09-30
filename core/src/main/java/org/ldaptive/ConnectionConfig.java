/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;
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
  private Duration connectTimeout = Duration.ofMinutes(1);

  /** Duration of time to wait for responses. */
  private Duration responseTimeout = Duration.ofMinutes(1);

  /** Whether to automatically reconnect to the server when a connection is lost. Default is true. */
  private boolean autoReconnect = true;

  /**
   * Condition used to determine whether another reconnect attempt should be made. Default makes a single attempt only
   * if the connection was previously opened.
   */
  private Predicate<RetryMetadata> autoReconnectCondition =
    metadata -> metadata instanceof ClosedRetryMetadata && metadata.getAttempts() == 0;

  /** Whether pending operations should be replayed after a reconnect. Default is true. */
  private boolean autoReplay = true;

  /** Configuration for SSL and startTLS connections. */
  private SslConfig sslConfig;

  /** Connect to LDAP using startTLS. */
  private boolean useStartTLS;

  /** Connection initializers to execute on {@link Connection#open()}. */
  private ConnectionInitializer[] connectionInitializers;

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
   * Returns the connect timeout.
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
   * Returns the response timeout.
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
   * Returns whether connections will attempt to reconnect.
   *
   * @return  whether to automatically reconnect when a connection is lost
   */
  public boolean getAutoReconnect()
  {
    return autoReconnect;
  }


  /**
   * Sets whether connections will attempt to reconnect when unexpectedly closed.
   *
   * @param  b  whether to automatically reconnect when a connection is lost
   */
  public void setAutoReconnect(final boolean b)
  {
    checkImmutable();
    logger.trace("setting autoReconnect: {}", b);
    autoReconnect = b;
  }


  /**
   * Returns the auto reconnect condition.
   *
   * @return  auto reconnect condition
   */
  public Predicate<RetryMetadata> getAutoReconnectCondition()
  {
    return autoReconnectCondition;
  }


  /**
   * Sets the auto reconnect condition.
   *
   * @param  predicate  to determine whether to attempt a reconnect
   */
  public void setAutoReconnectCondition(final Predicate<RetryMetadata> predicate)
  {
    checkImmutable();
    logger.trace("setting autoReconnectCondition: {}", predicate);
    autoReconnectCondition = predicate;
  }


  /**
   * Returns whether operations should be replayed after a reconnect.
   *
   * @return  whether to auto replay
   */
  public boolean getAutoReplay()
  {
    return autoReplay;
  }


  /**
   * Sets whether operations will be replayed after a reconnect.
   *
   * @param  b  whether to replay operations
   */
  public void setAutoReplay(final boolean b)
  {
    checkImmutable();
    logger.trace("setting autoReplay: {}", b);
    autoReplay = b;
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
   * Returns the connection initializers.
   *
   * @return  connection initializers
   */
  public ConnectionInitializer[] getConnectionInitializers()
  {
    return connectionInitializers;
  }


  /**
   * Sets the connection initializers.
   *
   * @param  initializers  connection initializers
   */
  public void setConnectionInitializers(final ConnectionInitializer... initializers)
  {
    checkImmutable();
    logger.trace("setting connectionInitializers: {}", Arrays.toString(initializers));
    connectionInitializers = initializers;
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
   * @param  strategy  for making new connections
   */
  public void setConnectionStrategy(final ConnectionStrategy strategy)
  {
    checkImmutable();
    logger.trace("setting connectionStrategy: {}", strategy);
    connectionStrategy = strategy;
  }


  /**
   * Returns a new connection config initialized with the supplied config.
   *
   * @param  config  connection config to read properties from
   *
   * @return  connection config
   */
  public static ConnectionConfig copy(final ConnectionConfig config)
  {
    final ConnectionConfig cc = new ConnectionConfig();
    cc.setLdapUrl(config.getLdapUrl());
    cc.setConnectTimeout(config.getConnectTimeout());
    cc.setResponseTimeout(config.getResponseTimeout());
    cc.setAutoReconnect(config.getAutoReconnect());
    cc.setAutoReconnectCondition(config.getAutoReconnectCondition());
    cc.setAutoReplay(config.getAutoReplay());
    cc.setSslConfig(config.getSslConfig() != null ? SslConfig.copy(config.getSslConfig()) : null);
    cc.setUseStartTLS(config.getUseStartTLS());
    cc.setConnectionInitializers(config.getConnectionInitializers());
    cc.setConnectionStrategy(config.getConnectionStrategy());
    return cc;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("ldapUrl=").append(ldapUrl).append(", ")
      .append("connectTimeout=").append(connectTimeout).append(", ")
      .append("responseTimeout=").append(responseTimeout).append(", ")
      .append("autoReconnect=").append(autoReconnect).append(", ")
      .append("autoReconnectCondition=").append(autoReconnectCondition).append(", ")
      .append("autoReplay=").append(autoReplay).append(", ")
      .append("sslConfig=").append(sslConfig).append(", ")
      .append("useStartTLS=").append(useStartTLS).append(", ")
      .append("connectionInitializers=").append(Arrays.toString(connectionInitializers)).append(", ")
      .append("connectionStrategy=").append(connectionStrategy).toString();
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

    private final ConnectionConfig object = new ConnectionConfig();


    protected Builder() {}


    public Builder url(final String url)
    {
      object.setLdapUrl(url);
      return this;
    }


    public Builder connectTimeout(final Duration timeout)
    {
      object.setConnectTimeout(timeout);
      return this;
    }


    public Builder responseTimeout(final Duration timeout)
    {
      object.setResponseTimeout(timeout);
      return this;
    }


    public Builder autoReconnect(final boolean b)
    {
      object.setAutoReconnect(b);
      return this;
    }


    public Builder autoReconnectCondition(final Predicate<RetryMetadata> predicate)
    {
      object.setAutoReconnectCondition(predicate);
      return this;
    }


    public Builder autoReplay(final boolean b)
    {
      object.setAutoReplay(b);
      return this;
    }


    public Builder sslConfig(final SslConfig config)
    {
      object.setSslConfig(config);
      return this;
    }


    public Builder useStartTLS(final boolean b)
    {
      object.setUseStartTLS(b);
      return this;
    }


    public Builder connectionInitializers(final ConnectionInitializer... initializers)
    {
      object.setConnectionInitializers(initializers);
      return this;
    }


    public Builder strategy(final ConnectionStrategy strategy)
    {
      object.setConnectionStrategy(strategy);
      return this;
    }


    public ConnectionConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
