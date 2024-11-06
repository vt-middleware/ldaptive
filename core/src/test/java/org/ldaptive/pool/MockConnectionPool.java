/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.ldaptive.Connection;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.transport.mock.MockConnection;

/**
 * Mock connection pool for testing.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of response
 *
 * @author  Middleware Services
 */
public class MockConnectionPool<Q extends Request, S extends Result> implements ConnectionPool
{

  /** Available connections. */
  private final List<MockConnection<Q, S>> availableConns = new ArrayList<>();

  /** Active connections. */
  private final List<MockConnection<Q, S>> activeConns = new ArrayList<>();


  /**
   * Creates a new mock connection pool.
   *
   * @param  available  available connections
   * @param  active  active connections
   */
  public MockConnectionPool(final List<MockConnection<Q, S>> available, final List<MockConnection<Q, S>> active)
  {
    availableConns.addAll(available);
    activeConns.addAll(active);
  }


  @Override
  public void initialize() {}


  @Override
  public Connection getConnection()
    throws PoolException
  {
    return null;
  }


  @Override
  public int availableCount()
  {
    return availableConns.size();
  }


  @Override
  public int activeCount()
  {
    return activeConns.size();
  }


  @Override
  public Set<PooledConnectionStatistics> getPooledConnectionStatistics()
  {
    throw new UnsupportedOperationException();
  }


  @Override
  public void close() {}
}
