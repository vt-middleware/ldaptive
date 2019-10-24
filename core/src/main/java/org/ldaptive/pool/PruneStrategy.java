/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.util.function.Function;

/**
 * Provides an interface for pruning connections from the pool.
 *
 * @author  Middleware Services
 */
public interface PruneStrategy extends Function<PooledConnectionProxy, Boolean>
{


  /**
   * Returns the number of statistics to store for this prune strategy. See {@link PooledConnectionStatistics}.
   *
   * @return  number of statistics to store
   */
  int getStatisticsSize();


  /**
   * Returns the interval at which the prune task will be executed.
   *
   * @return  prune period
   */
  Duration getPrunePeriod();
}
