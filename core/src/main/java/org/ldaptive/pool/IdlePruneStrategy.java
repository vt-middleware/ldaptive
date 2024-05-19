/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;

/**
 * Removes connections from the pool based on how long they have been idle in the available queue. By default, this
 * implementation executes every 5 minutes and prunes connections that have been idle for more than 10 minutes.
 *
 * @author  Middleware Services
 */
public class IdlePruneStrategy extends AbstractPruneStrategy
{

  /** Default number of statistics to store. Value is {@value}. */
  private static final int DEFAULT_STATISTICS_SIZE = 1;

  /** Default idle time. Value is 10 minutes. */
  private static final Duration DEFAULT_IDLE_TIME = Duration.ofMinutes(10);

  /** Idle time. */
  private Duration idleTime;


  /** Creates a new idle prune strategy. */
  public IdlePruneStrategy()
  {
    this(DEFAULT_PRUNE_PERIOD, DEFAULT_IDLE_TIME);
  }


  /**
   * Creates a new idle prune strategy. Sets the prune period to half of the supplied idle time.
   *
   * @param  idle  time at which a connection should be pruned
   */
  public IdlePruneStrategy(final Duration idle)
  {
    setPrunePeriod(idle.dividedBy(2));
    setIdleTime(idle);
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
    final Instant timeAvailable = conn.getPooledConnectionStatistics().getLastAvailableStat();
    logger.trace("evaluating timestamp {} for connection {}", timeAvailable, conn);
    return timeAvailable == null || timeAvailable.plus(idleTime).isBefore(Instant.now());
  }


  @Override
  public int getStatisticsSize()
  {
    return DEFAULT_STATISTICS_SIZE;
  }


  /**
   * Returns the idle time.
   *
   * @return  idle time
   */
  public final Duration getIdleTime()
  {
    return idleTime;
  }


  /**
   * Sets the idle time.
   *
   * @param  time  that a connection has been idle and should be pruned
   */
  public final void setIdleTime(final Duration time)
  {
    assertMutable();
    if (time == null || time.isNegative()) {
      throw new IllegalArgumentException("Idle time cannot be null or negative");
    }
    idleTime = time;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "prunePeriod=" + getPrunePeriod() + ", " +
      "idleTime=" + idleTime + "]";
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static IdlePruneStrategy.Builder builder()
  {
    return new IdlePruneStrategy.Builder();
  }


  /** Idle prune strategy builder. */
  public static class Builder extends
    AbstractPruneStrategy.AbstractBuilder<IdlePruneStrategy.Builder, IdlePruneStrategy>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new IdlePruneStrategy());
    }


    @Override
    protected IdlePruneStrategy.Builder self()
    {
      return this;
    }


    /**
     * Sets the prune idle time.
     *
     * @param  time  to set
     *
     * @return  this builder
     */
    public IdlePruneStrategy.Builder idle(final Duration time)
    {
      object.setIdleTime(time);
      return self();
    }
  }
}
