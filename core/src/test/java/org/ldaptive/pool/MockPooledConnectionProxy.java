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

  /** Connection. */
  private final Connection connection;

  /** Pooled connection statistics. */
  private final PooledConnectionStatistics connectionStatistics;

  /** Time this connection was created. */
  private final Instant createdTime;


  /**
   * Creates a new test pooled connection proxy.
   *
   * @param  conn  connection
   * @param  statistics  connection statistics
   */
  public MockPooledConnectionProxy(final Connection conn, final PooledConnectionStatistics statistics)
  {
    this(conn, statistics, Instant.now());
  }


  /**
   * Creates a new test pooled connection proxy.
   *
   * @param  conn  connection
   * @param  statistics  connection statistics
   * @param  time  creation time
   */
  MockPooledConnectionProxy(final Connection conn, final PooledConnectionStatistics statistics, final Instant time)
  {
    connection = conn;
    connectionStatistics = statistics;
    createdTime = time;
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
  public Object invoke(final Object proxy, final Method method, final Object[] args)
  {
    throw new UnsupportedOperationException();
  }
}
