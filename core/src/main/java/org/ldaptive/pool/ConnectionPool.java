/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.Set;
import org.ldaptive.Connection;

/**
 * Provides an interface for connection pooling.
 *
 * @author  Middleware Services
 */
public interface ConnectionPool
{


  /** Initialize this pool for use. */
  void initialize();


  /**
   * Returns an object from the pool.
   *
   * @return  pooled object
   *
   * @throws  PoolException  if this operation fails
   * @throws  BlockingTimeoutException  if this pool is configured with a block time and it occurs
   */
  Connection getConnection()
    throws PoolException;


  /**
   * Returns the number of connections available for use.
   *
   * @return  count
   */
  int availableCount();


  /**
   * Returns the number of connections in use.
   *
   * @return  count
   */
  int activeCount();


  /**
   * Returns the statistics for each connection in the pool.
   *
   * @return  connection statistics
   */
  Set<PooledConnectionStatistics> getPooledConnectionStatistics();


  /** Empty this pool, freeing any resources. */
  void close();
}
