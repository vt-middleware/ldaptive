/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes connections from the pool based on how long they have been idle in the available queue. By default this
 * implementation executes every 5 minutes and prunes connections that have been idle for more than 10 minutes.
 *
 * @author  Middleware Services
 */
public class IdlePruneStrategy implements PruneStrategy
{

  /** Default number of statistics to store. Value is {@value}. */
  private static final int DEFAULT_STATISTICS_SIZE = 1;

  /** Default prune period in seconds. Value is 5 minutes. */
  private static final Duration DEFAULT_PRUNE_PERIOD = Duration.ofMinutes(5);

  /** Default idle time. Value is 10 minutes. */
  private static final Duration DEFAULT_IDLE_TIME = Duration.ofMinutes(10);

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Prune period. */
  private Duration prunePeriod;

  /** Idle time. */
  private Duration idleTime;


  /** Creates a new idle prune strategy. */
  public IdlePruneStrategy()
  {
    this(DEFAULT_PRUNE_PERIOD, DEFAULT_IDLE_TIME);
  }


  /**
   * Creates a new idle prune strategy.
   *
   * @param  period  to execute the prune task
   * @param  idle  time at which a connection should be pruned
   */
  public IdlePruneStrategy(final Duration period, final Duration idle)
  {
    setPrunePeriod(period);
    setIdleTime(idle);
  }


  @Override
  public Boolean apply(final PooledConnectionProxy conn)
  {
    final Instant timeAvailable = conn.getPooledConnectionStatistics().getLastAvailableState();
    logger.trace("evaluating timestamp {} for connection {}", timeAvailable, conn);
    return timeAvailable.plus(idleTime).isBefore(Instant.now());
  }


  @Override
  public int getStatisticsSize()
  {
    return DEFAULT_STATISTICS_SIZE;
  }


  @Override
  public Duration getPrunePeriod()
  {
    return prunePeriod;
  }


  /**
   * Sets the prune period.
   *
   * @param  period  to set
   */
  public void setPrunePeriod(final Duration period)
  {
    if (period == null || period.isNegative()) {
      throw new IllegalArgumentException("Prune period cannot be null or negative");
    }
    prunePeriod = period;
  }


  /**
   * Returns the idle time.
   *
   * @return  idle time
   */
  public Duration getIdleTime()
  {
    return idleTime;
  }


  /**
   * Sets the idle time.
   *
   * @param  time  that a connection has been idle and should be pruned
   */
  public void setIdleTime(final Duration time)
  {
    if (time == null || time.isNegative()) {
      throw new IllegalArgumentException("Idle time cannot be null or negative");
    }
    idleTime = time;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("prunePeriod=").append(prunePeriod).append(", ")
      .append("idleTime=").append(idleTime).append("]").toString();
  }
}
