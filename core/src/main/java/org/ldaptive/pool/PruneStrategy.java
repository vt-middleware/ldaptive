/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Provides an interface for pruning connections from the pool. The list of predicates provided by the strategy are used
 * against the entire pool in order. So that connections pruned by the first predicate are no longer available when the
 * second predicate is used. The input to the predicate is the connection pool and its minimum pool size. In this
 * fashion, a prune strategy can order any number of predicates, each of which can inspect the pool and make a
 * determination which to prune a connection.
 *
 * @author  Middleware Services
 */
public interface PruneStrategy extends Function<PooledConnectionProxy, List<Predicate<PoolState>>>
{


  /**
   * Returns the number of statistics to store for this prune strategy. See {@link PooledConnectionStatistics}.
   *
   * @return  number of statistics to store
   */
  int getStatisticsSize();


  /**
   * Returns the number of predicates this prune strategy uses. Must match the size of the list returned by
   * {@link #apply(Object)}.
   *
   * @return  number of predicates this prune strategy uses
   */
  int getPredicateSize();


  /**
   * Returns the interval at which the prune task will be executed.
   *
   * @return  prune period
   */
  Duration getPrunePeriod();
}
