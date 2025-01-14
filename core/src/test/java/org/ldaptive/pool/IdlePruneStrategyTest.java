/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
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
  private final MockConnection p0Conn =
    MockConnection.builder(
      ConnectionConfig.builder().url("ldap://ds1.ldaptive.org ldap://ds2.ldaptive.org ldap://ds3.ldaptive.org").build())
      .openPredicate(url -> url.getHostname().startsWith("ds1"))
      .build();

  /** Mock connection for testing. */
  private final MockConnection p1Conn =
    MockConnection.builder(
      ConnectionConfig.builder().url("ldap://ds1.ldaptive.org ldap://ds2.ldaptive.org ldap://ds3.ldaptive.org").build())
      .openPredicate(url -> url.getHostname().startsWith("ds2"))
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
    p0Conn.open();
    p1Conn.open();

    // create connection pools for testing
    final MockConnectionPool<?, ?> idlePool = new MockConnectionPool<>(
      List.of(p0Conn, p0Conn, p0Conn), List.of());

    final MockConnectionPool<?, ?> idlePrunePool = new MockConnectionPool<>(
      List.of(p0Conn, p0Conn, p0Conn, p0Conn, p0Conn, p0Conn), List.of());

    final MockConnectionPool<?, ?> activePool = new MockConnectionPool<>(
      List.of(), List.of(p0Conn, p0Conn, p0Conn));

    final MockConnectionPool<?, ?> activePrunePool = new MockConnectionPool<>(
      List.of(p0Conn, p0Conn, p0Conn, p0Conn, p0Conn, p0Conn),
      List.of(p0Conn, p0Conn, p0Conn));

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
          new MockPooledConnectionProxy(idlePool, p1Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong),
          IdlePruneStrategy.builder().priority(1).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .priorityFactor(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .priorityFactor(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .priorityFactor(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder()
            .priority(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder()
            .priority(1)
            .priorityFactor(1)
            .age(Duration.ofMinutes(5))
            .idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p1Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        // idlePrunePool should exercise both idle and age pruning
        new Object[] {
          new MockPooledConnectionProxy(idlePrunePool, p0Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePrunePool, p0Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePrunePool, p0Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(idlePrunePool, p0Conn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePrunePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        // activePool should only exercise age pruning
        new Object[] {
          new MockPooledConnectionProxy(activePool, p0Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePool, p0Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePool, p0Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePool, p0Conn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        // activePrunePool should exercise both idle and age pruning
        new Object[] {
          new MockPooledConnectionProxy(activePrunePool, p0Conn, new PooledConnectionStatistics(0)),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePrunePool, p0Conn, noLastAvailableStat),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePrunePool, p0Conn, notIdleLongEnough),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(activePrunePool, p0Conn, idleTooLong),
          IdlePruneStrategy.builder().idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, noLastAvailableStat, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, notIdleLongEnough, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(6))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            activePrunePool, p0Conn, idleTooLong, Instant.now().minus(Duration.ofMinutes(3))),
          IdlePruneStrategy.builder().age(Duration.ofMinutes(5)).idle(Duration.ofMinutes(1)).build(),
          true,
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * Unit test for {@link IdlePruneStrategy#getPruneConditions()}.
   *
   * @param  conn  to apply to strategy
   * @param  strategy  prune strategy
   * @param  result  expected result of the strategy
   */
  @Test(groups = "conn", dataProvider = "conns")
  public void apply(final PooledConnectionProxy conn, final IdlePruneStrategy strategy, final boolean result)
  {
    boolean b = false;
    for (Predicate<PooledConnectionProxy> p : strategy.getPruneConditions()) {
      b = p.test(conn);
      if (b) {
        break;
      }
    }
    assertThat(b)
      .withFailMessage("Prune %s for strategy %s and URL %s", b, strategy, conn.getConnection().getLdapURL())
      .isEqualTo(result);
  }
}
