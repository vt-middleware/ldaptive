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
 * Unit test for {@link AgePruneStrategy}.
 *
 * @author  Middleware Services
 */
public class AgePruneStrategyTest
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

  /** Mock connection for testing. */
  private final MockConnection p2Conn =
    MockConnection.builder(
      ConnectionConfig.builder().url("ldap://ds1.ldaptive.org ldap://ds2.ldaptive.org ldap://ds3.ldaptive.org").build())
      .openPredicate(url -> url.getHostname().startsWith("ds3"))
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
    p0Conn.open();
    p1Conn.open();
    p2Conn.open();

    final MockConnectionPool<?, ?> idlePool = new MockConnectionPool<>(
      List.of(p0Conn, p0Conn, p0Conn), List.of());

    return
      new Object[][] {
        // created=PT6D, no ageTime, priority=2
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ZERO).build(),
          false,
        },
        // created=PT6D, no ageTime, priority=1
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ZERO).build(),
          false,
        },
        // created=PT6D, no ageTime, priority=0
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ZERO).build(),
          false,
        },
        // created=PT6D, no ageTime, priority=-1
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofDays(6))),
          AgePruneStrategy.builder().age(Duration.ZERO).build(),
          false,
        },
        // created=PT6M, ageTime=PT5M, priority=2
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(2).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT6M, ageTime=PT5M, priority=1
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT6M, ageTime=PT5M, priority=0
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().priority(0).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT6M, ageTime=PT5M, priority=-1
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(6))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT3M, ageTime=PT5M, priority=2, (age / priority * 1)
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(2).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(2).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(2).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT3M, ageTime=PT5M, priority=1, (age / priority * 1)
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(1).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(1).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(1).priorityFactor(1).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT3M, ageTime=PT5M, priority=0, (age / priority * 2)
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(0).priorityFactor(2).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(0).priorityFactor(2).age(Duration.ofMinutes(5)).build(),
          true,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().priority(0).priorityFactor(2).age(Duration.ofMinutes(5)).build(),
          true,
        },
        // created=PT3M, ageTime=PT5M, priority=-1
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p0Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p1Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          false,
        },
        new Object[] {
          new MockPooledConnectionProxy(
            idlePool, p2Conn, new PooledConnectionStatistics(0), Instant.now().minus(Duration.ofMinutes(3))),
          AgePruneStrategy.builder().age(Duration.ofMinutes(5)).build(),
          false,
        },
      };
  }


  /**
   * Unit test for {@link AgePruneStrategy#getPruneConditions()}.
   *
   * @param  conn  to apply to strategy
   * @param  strategy  prune strategy
   * @param  result  expected result of the strategy
   */
  @Test(groups = "conn", dataProvider = "conns")
  public void apply(final PooledConnectionProxy conn, final AgePruneStrategy strategy, final boolean result)
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
