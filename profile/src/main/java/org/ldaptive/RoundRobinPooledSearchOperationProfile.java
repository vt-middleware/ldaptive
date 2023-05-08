/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Class for profiling multiple instances of {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class RoundRobinPooledSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 30;


  @Override
  // CheckStyle:MagicNumber OFF
  protected void initialize(final String host, final int port)
  {
    connectionFactory = PooledConnectionFactory.builder()
      .config(ConnectionConfig.builder()
        .url(
          String.format(
            "%s %s %s",
            new LdapURL(host, port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-2", port).getHostnameWithSchemeAndPort(),
            new LdapURL(host + "-3", port).getHostnameWithSchemeAndPort()))
        .connectTimeout(Duration.ofSeconds(5))
        .connectionStrategy(new RoundRobinConnectionStrategy())
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(iterations > 0 ? Duration.ofSeconds(5) : Duration.ofSeconds(threadSleep / 2))
      .min(POOL_SIZE)
      .max(POOL_SIZE)
      .validator(SearchConnectionValidator.builder()
        .period(Duration.ofMinutes(1))
        .build())
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();
  }
  // CheckStyle:MagicNumber ON


  @Override
  protected void shutdown()
  {
    connectionFactory.close();
  }


  @Override
  protected int doOperation(final Consumer<Object> consumer, final int uid)
  {
    doOperation(connectionFactory, consumer, uid);
    return 1;
  }


  @Override
  public String toString()
  {
    return (connectionFactory != null ? connectionFactory.toString() : "[null connection factory]");
  }
}
