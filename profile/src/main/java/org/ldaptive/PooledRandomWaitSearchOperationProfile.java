/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Random;
import java.util.function.Consumer;
import org.ldaptive.pool.IdlePruneStrategy;

/**
 * Class for profiling {@link PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public final class PooledRandomWaitSearchOperationProfile extends AbstractSearchOperationProfile
{

  /** Default pool size. */
  private static final int POOL_SIZE = 10;

  /** For generating random wait times. */
  private final Random random = new Random();

  /** */
  private final Duration period = Duration.ofSeconds(10);


  @Override
  // CheckStyle:MagicNumber OFF
  protected void initialize(final String host, final int port)
  {
    connectionFactory = PooledConnectionFactory.builder()
      .name(host)
      .pruneStrategy(IdlePruneStrategy.builder()
        .idle(period.dividedBy(4))
        .period(period)
        .build())
      .validatePeriodically(true)
      .validator(SearchConnectionValidator.builder()
        .validResultCodes(ResultCode.LOCAL_ERROR)
        .period(period.multipliedBy(3))
        .build())
      .config(ConnectionConfig.builder()
        .url(new LdapURL(host, port).getHostnameWithSchemeAndPort())
        .connectTimeout(Duration.ofSeconds(5))
        .connectionInitializers(
          BindConnectionInitializer.builder()
            .dn(bindDn)
            .credential(bindCredential)
            .build())
        .build())
      .blockWaitTime(iterations > 0 ? Duration.ofSeconds(5) : Duration.ofSeconds(threadSleep / 2))
      .failFastInitialize(false)
      .min(3)
      .max(POOL_SIZE)
      .build();
    ((PooledConnectionFactory) connectionFactory).initialize();
  }
  // CheckStyle:MagicNumber ON


  @Override
  // CheckStyle:MagicNumber OFF
  protected int doOperation(final Consumer<Object> consumer, final int uid)
  {
    try {
      Thread.sleep(random.nextInt(1000));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return super.doOperation(consumer, uid);
  }
  // CheckStyle:MagicNumber ON
}
