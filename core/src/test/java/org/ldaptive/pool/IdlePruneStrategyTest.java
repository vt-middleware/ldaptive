/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import org.ldaptive.Connection;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link IdlePruneStrategy}.
 *
 * @author  Middleware Services
 */
public class IdlePruneStrategyTest
{


  /**
   * Prune strategy test data.
   *
   * @return  test data
   */
  @DataProvider(name = "conns")
  public Object[][] createConnections()
  {
    final PooledConnectionStatistics noLastAvailableStat = new PooledConnectionStatistics(1);
    final PooledConnectionStatistics notIdleLongEnough = new PooledConnectionStatistics(1);
    notIdleLongEnough.addAvailableStat(Instant.now().minusSeconds(10));
    final PooledConnectionStatistics idleTooLong = new PooledConnectionStatistics(1);
    idleTooLong.addAvailableStat(Instant.now().minusSeconds(120));
    return
      new Object[][] {
        new Object[] {
          new TestPooledConnectionProxy(noLastAvailableStat),
          Duration.ofMinutes(1),
          true,
        },
        new Object[] {
          new TestPooledConnectionProxy(notIdleLongEnough),
          Duration.ofMinutes(1),
          false,
        },
        new Object[] {
          new TestPooledConnectionProxy(idleTooLong),
          Duration.ofMinutes(1),
          true,
        },
      };
  }


  /**
   * Unit test for {@link IdlePruneStrategy#apply(PooledConnectionProxy)}.
   *
   * @param  conn  to apply to strategy
   * @param  idle  strategy idle duration
   * @param  result  expected result of the strategy
   */
  @Test(groups = "conn", dataProvider = "conns")
  public void apply(final PooledConnectionProxy conn, final Duration idle, final boolean result)
  {
    final IdlePruneStrategy strategy = new IdlePruneStrategy(idle);
    Assert.assertEquals(strategy.apply(conn), result);
  }


  /** Class to support testing of a {@link PooledConnectionProxy}. */
  private static class TestPooledConnectionProxy implements PooledConnectionProxy
  {

    /** Pooled connection statistics. */
    private final PooledConnectionStatistics connectionStatistics;

    /**
     * Creates a new test pooled connection proxy.
     *
     * @param  statistics  connection statistics
     */
    TestPooledConnectionProxy(final PooledConnectionStatistics statistics)
    {
      connectionStatistics = statistics;
    }


    @Override
    public ConnectionPool getConnectionPool()
    {
      throw new UnsupportedOperationException();
    }


    @Override
    public Connection getConnection()
    {
      throw new UnsupportedOperationException();
    }


    @Override
    public long getCreatedTime()
    {
      throw new UnsupportedOperationException();
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
}
