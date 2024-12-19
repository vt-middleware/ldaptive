/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Removes connections from the pool based on how long they have been idle in the available queue. By default, this
 * implementation executes every 5 minutes and prunes connections that have been idle for more than 10 minutes. This
 * strategy will not prune available connections below the minimum pool size unless an age time is configured.
 * Connections will be pruned by age before they are pruned by idle time.
 *
 * @author  Middleware Services
 */
public class IdlePruneStrategy extends AgePruneStrategy
{

  /** Default idle time. Value is 10 minutes. */
  private static final Duration DEFAULT_IDLE_TIME = Duration.ofMinutes(10);

  /** Idle time. */
  private Duration idleTime;


  /** Creates a new idle prune strategy. */
  public IdlePruneStrategy()
  {
    this(DEFAULT_IDLE_TIME);
  }


  /**
   * Creates a new idle prune strategy. Sets the prune period to half of the supplied idle time.
   *
   * @param  idle  time at which a connection should be pruned
   */
  public IdlePruneStrategy(final Duration idle)
  {
    this(idle.dividedBy(2), idle);
  }


  /**
   * Creates a new idle prune strategy.
   *
   * @param  period  to execute the prune task
   * @param  idle  time at which a connection should be pruned
   */
  public IdlePruneStrategy(final Duration period, final Duration idle)
  {
    this(period, idle, Duration.ZERO);
  }


  /**
   * Creates a new idle prune strategy.
   *
   * @param  period  to execute the prune task
   * @param  idle  time at which a connection should be pruned
   * @param  age  time at which a connection should be pruned
   */
  public IdlePruneStrategy(final Duration period, final Duration idle, final Duration age)
  {
    setPrunePeriod(period);
    setIdleTime(idle);
    setAgeTime(age);
  }


  @Override
  public List<Predicate<PooledConnectionProxy>> getPruneConditions()
  {
    final List<Predicate<PooledConnectionProxy>> predicates = new ArrayList<>(super.getPruneConditions());
    predicates.add(proxy -> {
      final int totalSize = proxy.getConnectionPool().activeCount() + proxy.getConnectionPool().availableCount();
      final int numConnAboveMin = totalSize - proxy.getMinPoolSize();
      final int numConnToPrune = Math.min(proxy.getConnectionPool().availableCount(), numConnAboveMin);
      logger.trace("number of connections to prune {}", numConnToPrune);
      if (numConnToPrune > 0) {
        final Instant timeAvailable = proxy.getPooledConnectionStatistics().getLastAvailableStat();
        logger.trace("evaluating time available {} for connection {}", timeAvailable, proxy);
        return timeAvailable == null || timeAvailable.plus(idleTime).isBefore(Instant.now());
      }
      return false;
    });
    return Collections.unmodifiableList(predicates);
  }


  @Override
  public int getStatisticsSize()
  {
    return 1;
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
      "ageTime=" + getAgeTime() + ", " +
      "prunePriority=" + getPrunePriority() + ", " +
      "idleTime=" + idleTime + "]";
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** Idle prune strategy builder. */
  public static class Builder extends AgePruneStrategy.Builder
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new IdlePruneStrategy());
    }


    @Override
    protected Builder self()
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
    public Builder idle(final Duration time)
    {
      ((IdlePruneStrategy) object).setIdleTime(time);
      return self();
    }


    @Override
    public Builder age(final Duration time)
    {
      object.setAgeTime(time);
      return self();
    }


    @Override
    public Builder priority(final int i)
    {
      object.setPrunePriority(i);
      return self();
    }
  }
}
