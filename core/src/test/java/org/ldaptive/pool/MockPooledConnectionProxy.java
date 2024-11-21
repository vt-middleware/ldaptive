/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.Method;
import java.time.Instant;
import org.ldaptive.Connection;

/**
 * {@link PooledConnectionProxy} used for testing.
 *
 * @author  Middleware Services
 */
public class MockPooledConnectionProxy implements PooledConnectionProxy
{

  /** Connection pool */
  private final ConnectionPool connectionPool;

  /** Connection. */
  private final Connection connection;

  /** Pooled connection statistics. */
  private final PooledConnectionStatistics connectionStatistics;

  /** Time this connection was created. */
  private final Instant createdTime;


  /**
   * Creates a new test pooled connection proxy.
   *
   * @param  pool  connection pool
   * @param  conn  connection
   * @param  statistics  connection statistics
   */
  public MockPooledConnectionProxy(
    final ConnectionPool pool, final Connection conn, final PooledConnectionStatistics statistics)
  {
    this(pool, conn, statistics, Instant.now());
  }


  /**
   * Creates a new test pooled connection proxy.
   *
   * @param  pool  connection pool
   * @param  conn  connection
   * @param  statistics  connection statistics
   * @param  time  creation time
   */
  MockPooledConnectionProxy(
    final ConnectionPool pool, final Connection conn, final PooledConnectionStatistics statistics, final Instant time)
  {
    connectionPool = pool;
    connection = conn;
    connectionStatistics = statistics;
    createdTime = time;
  }


  @Override
  public ConnectionPool getConnectionPool()
  {
    return connectionPool;
  }


  @Override
  public Connection getConnection()
  {
    return connection;
  }


  @Override
  public Instant getCreatedTime()
  {
    return createdTime;
  }


  @Override
  public PooledConnectionStatistics getPooledConnectionStatistics()
  {
    return connectionStatistics;
  }


  @Override
  public int getMinPoolSize()
  {
    // CheckStyle:MagicNumber OFF
    return 3;
    // CheckStyle:MagicNumber ON
  }


  @Override
  public int getMaxPoolSize()
  {
    // CheckStyle:MagicNumber OFF
    return 10;
    // CheckStyle:MagicNumber ON
  }


  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
  {
    throw new UnsupportedOperationException();
  }
}
