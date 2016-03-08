/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;

/**
 * Provides an interface for pruning connections from the pool.
 *
 * @author  Middleware Services
 */
public interface PruneStrategy
{


  /**
   * Invoked to determine whether a connection should be pruned from the pool.
   *
   * @param  conn  that is available for pruning
   *
   * @return  whether the connection should be pruned
   */
  boolean prune(PooledConnectionProxy conn);


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
