/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Provides an interface for pruning connections from the pool. The list of predicates provided by the strategy are used
 * against the entire pool in order. So that connections pruned by the first predicate are no longer available when the
 * second predicate is used. In this fashion, a prune strategy can order any number of predicates, each of which can
 * inspect the pool and make a determination whether to prune a connection.
 *
 * @author  Middleware Services
 */
public interface PruneStrategy extends Consumer<Supplier<Iterator<PooledConnectionProxy>>>
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
