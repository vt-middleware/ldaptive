/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Class for profiling multiple instances of {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class MultiPooledSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;

  /** Connection factory. */
  protected ConnectionFactory connectionFactory2;

  /** Connection factory. */
  protected ConnectionFactory connectionFactory3;


  @Override
  // CheckStyle:MagicNumber OFF
  protected void initialize(final String host, final int port)
  {
    connectionFactory = PooledConnectionFactory.builder()
      .name(host)
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getHostnameWithSchemeAndPort())
        .connectTimeout(Duration.ofSeconds(5))
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(iterations > 0 ? Duration.ofSeconds(5) : Duration.ofMillis(threadSleep / 2))
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();

    connectionFactory2 = PooledConnectionFactory.builder()
      .name(host + "-2")
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host + "-2", port).getHostnameWithSchemeAndPort())
        .connectTimeout(Duration.ofSeconds(5))
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(iterations > 0 ? Duration.ofSeconds(5) : Duration.ofMillis(threadSleep / 2))
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory2).initialize();

    connectionFactory3 = PooledConnectionFactory.builder()
      .name(host + "-3")
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host + "-3", port).getHostnameWithSchemeAndPort())
        .connectTimeout(Duration.ofSeconds(5))
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(iterations > 0 ? Duration.ofSeconds(5) : Duration.ofMillis(threadSleep / 2))
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory3).initialize();
  }
  // CheckStyle:MagicNumber ON


  @Override
  protected void shutdown()
  {
    connectionFactory.close();
    connectionFactory2.close();
    connectionFactory3.close();
  }


  @Override
  protected void doOperation(final Consumer<Object> consumer, final int uid)
  {
    doOperation(connectionFactory, consumer, uid);
    doOperation(connectionFactory2, consumer, uid);
    doOperation(connectionFactory3, consumer, uid);
  }


  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();
    sb.append(connectionFactory != null ? connectionFactory.toString() : "[null connection factory]").append(" -- ");
    sb.append(connectionFactory2 != null ? connectionFactory2.toString() : "[null connection factory2]").append(" -- ");
    sb.append(connectionFactory3 != null ? connectionFactory3.toString() : "[null connection factory3]");
    return sb.toString();
  }
}
