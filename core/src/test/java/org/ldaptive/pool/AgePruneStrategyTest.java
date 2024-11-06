/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.transport.mock.MockConnection;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link AgePruneStrategy}.
 *
 * @author  Middleware Services
 */
public class AgePruneStrategyTest
{

  /** Mock connection for testing. */
  private final MockConnection activeConn =
    MockConnection.builder(ConnectionConfig.builder().url("ldap://ds1.ldaptive.org ldap://ds2.ldaptive.org").build())
      .openPredicate(url -> true)
      .build();

  /** Mock connection for testing. */
  private final MockConnection passiveConn =
    MockConnection.builder(ConnectionConfig.builder().url("ldap://ds1.ldaptive.org ldap://ds2.ldaptive.org").build())
      .openPredicate(url -> !url.getHostname().startsWith("ds1"))
      .build();


  /**
   * Prune strategy test data.
   *
   * @return  test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "conns")
  @SuppressWarnings("unchecked")
  public Object[][] createConnections()
    throws Exception
  {
    // initialize the LDAP URL in the connections
    activeConn.open();
    passiveConn.open();

    final MockConnectionPool<?, ?> idlePool = new MockConnectionPool<>(
      List.of(activeConn, activeConn, activeConn), List.of());

    return
      new Object[][] {
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).priority(1).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).priority(1).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).priority(1).build(),
          false,
        },
      };
  }


  /**
   * Unit test for {@link AgePruneStrategy#apply(PooledConnectionProxy)}.
   *
   * @param  pool  to prune
   * @param  conn  to apply to strategy
   * @param  strategy  prune strategy
   * @param  result  expected result of the strategy
   */
  @Test(groups = "conn", dataProvider = "conns")
  public void apply(
    final ConnectionPool pool, final PooledConnectionProxy conn, final AgePruneStrategy strategy, final boolean result)
  {
    assertThat(strategy.apply(conn).stream().anyMatch(p -> p.test(new DefaultPoolState(pool, 3, 10))))
      .isEqualTo(result);
  }
}
