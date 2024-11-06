/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Default implementation of {@link PoolState}.
 *
 * @author  Middleware Services
 */
final class DefaultPoolState implements PoolState
{

  /** Connection pool. */
  private final ConnectionPool connectionPool;

  /** Minimum pool size. */
  private final int minPoolSize;

  /** Maximum pool size. */
  private final int maxPoolSize;


  /**
   * Creates a new prune pool state.
   *
   * @param  pool  connection pool
   * @param  min  the minimum pool size
   * @param  max  the maximum pool size
   */
  DefaultPoolState(final ConnectionPool pool, final int min, final int max)
  {
    connectionPool = pool;
    minPoolSize = min;
    maxPoolSize = max;
  }


  @Override
  public int getMinPoolSize()
  {
    return minPoolSize;
  }


  @Override
  public int getMaxPoolSize()
  {
    return maxPoolSize;
  }


  @Override
  public int getAvailableCount()
  {
    return connectionPool.availableCount();
  }


  @Override
  public int getActiveCount()
  {
    return connectionPool.activeCount();
  }
}
