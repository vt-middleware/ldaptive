/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Interface that defines the pool properties that can be used by a {@link PruneStrategy} to prune connections.
 *
 * @author  Middleware Services
 */
public interface PoolState
{


  /**
   * Returns the minimum size of the connection pool.
   *
   * @return  minimum size of the connection pool
   */
  int getMinPoolSize();


  /**
   * Returns the maximum size of the connection pool.
   *
   * @return  maximum size of the connection pool
   */
  int getMaxPoolSize();


  /**
   * Returns the number of connection in the available queue.
   *
   * @return  number of connection in the available queue.
   */
  int getAvailableCount();


  /**
   * Returns the number of connections in the active queue
   *
   * @return  number of connections in the active queue
   */
  int getActiveCount();


  /**
   * Returns the number of connections in both the available and active queues.
   *
   * @return  number of connections in both the available and active queues
   */
  default int getTotalCount()
  {
    return getAvailableCount() + getActiveCount();
  }
}
