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
 * Unit test for {@link IdlePruneStrategy}.
 *
 * @author  Middleware Services
 */
public class IdlePruneStrategyTest
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
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "conns")
  @SuppressWarnings("unchecked")
  public Object[][] createConnections()
    throws Exception
  {
    // initialize the LDAP URL in the connections
    activeConn.open();
    passiveConn.open();

    // create connection pools for testing
    final MockConnectionPool<?, ?> idlePool = new MockConnectionPool<>(
      List.of(activeConn, activeConn, activeConn), List.of());

    final MockConnectionPool<?, ?> idlePrunePool = new MockConnectionPool<>(
      List.of(activeConn, activeConn, activeConn, activeConn, activeConn, activeConn), List.of());

    final MockConnectionPool<?, ?> activePool = new MockConnectionPool<>(
      List.of(), List.of(activeConn, activeConn, activeConn));

    final MockConnectionPool<?, ?> activePrunePool = new MockConnectionPool<>(
      List.of(activeConn, activeConn, activeConn, activeConn, activeConn, activeConn),
      List.of(activeConn, activeConn, activeConn));

    // create various statistics for testing
    final PooledConnectionStatistics noLastAvailableStat = new PooledConnectionStatistics(1);
    final PooledConnectionStatistics notIdleLongEnough = new PooledConnectionStatistics(1);
    notIdleLongEnough.addAvailableStat(Instant.now().minusSeconds(10));
    final PooledConnectionStatistics idleTooLong = new PooledConnectionStatistics(1);
    idleTooLong.addAvailableStat(Instant.now().minusSeconds(120));

    return
      new Object[][] {
        // idlePool should only exercise age pruning and passive connections
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            passiveConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(passiveConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        // idlePrunePool should exercise both idle and age pruning
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(
            activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          idlePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        // activePool should only exercise age pruning
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        // activePrunePool should exercise both idle and age pruning
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(
            activeConn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          activePrunePool,
          new MockPooledConnectionProxy(activeConn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * Unit test for {@link IdlePruneStrategy#apply(PooledConnectionProxy)}.
   *
   * @param  pool  to prune
   * @param  conn  to apply to strategy
   * @param  strategy  prune strategy
   * @param  result  expected result of the strategy
   */
  @Test(groups = "conn", dataProvider = "conns")
  public void apply(
    final ConnectionPool pool, final PooledConnectionProxy conn, final IdlePruneStrategy strategy, final boolean result)
  {
    assertThat(strategy.apply(conn).stream().anyMatch(p -> p.test(new DefaultPoolState(pool, 3, 10))))
      .isEqualTo(result);
  }
}
