/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;

/**
 * Class for profiling {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class PooledSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;


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
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();
  }
  // CheckStyle:MagicNumber ON
}
