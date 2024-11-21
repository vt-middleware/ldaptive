/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

/**
 * Removes connections from the pool based on how long they have existed. By default, this implementation executes every
 * 30 minutes and prunes connections that have existed for more than 1 hour. Setting an age time of zero means that no
 * connections will be pruned by this strategy. This strategy will prune available connections below the minimum pool
 * size. This strategy will also only prune connections that have a higher priority if {@link #prunePriority} is set.
 * The higher the priority the quicker the connection will be removed.
 *
 * @author  Middleware Services
 */
public class AgePruneStrategy extends AbstractPruneStrategy
{

  /** Default age time. Value is 1 hour. */
  private static final Duration DEFAULT_AGE_TIME = Duration.ofHours(1);

  /** Age time. */
  private Duration ageTime;

  /**
   * Threshold at which to prune connections based on their priority. Connections with a priority &gt;= to this value
   * will be pruned.
   */
  private long prunePriority = -1L;


  /** Creates a new age prune strategy. */
  public AgePruneStrategy()
  {
    this(DEFAULT_AGE_TIME);
  }


  /**
   * Creates a new age prune strategy. Sets the prune period to half of the supplied age time.
   *
   * @param  age  time at which a connection should be pruned
   */
  public AgePruneStrategy(final Duration age)
  {
    this(age.dividedBy(2), age);
  }


  /**
   * Creates a new age prune strategy.
   *
   * @param  period  to execute the prune task
   * @param  age  time at which a connection should be pruned
   */
  public AgePruneStrategy(final Duration period, final Duration age)
  {
    setPrunePeriod(period);
    setAgeTime(age);
  }


  @Override
  public List<Predicate<PooledConnectionProxy>> getPruneConditions()
  {
    if (Duration.ZERO.equals(ageTime)) {
      return List.of(proxy -> false);
    }
    if (prunePriority >= 0) {
      return List.of(proxy -> {
        final long priority = proxy.getConnection().getLdapURL().getPriority();
        if (prunePriority >= priority) {
          final Instant timeCreated = proxy.getCreatedTime();
          logger.trace("evaluating created time {} for connection {}", timeCreated, proxy);
          return timeCreated == null || timeCreated.plus(ageTime.dividedBy(priority + 1)).isBefore(Instant.now());
        }
        return false;
      });
    }
    return List.of(proxy -> {
      final Instant timeCreated = proxy.getCreatedTime();
      return timeCreated == null || timeCreated.plus(ageTime).isBefore(Instant.now());
    });
  }


  @Override
  public int getStatisticsSize()
  {
    return 0;
  }


  /**
   * Returns the age time.
   *
   * @return  age time
   */
  public Duration getAgeTime()
  {
    return ageTime;
  }


  /**
   * Sets the age time.
   *
   * @param  time  since a connection has been created and should be pruned
   */
  public void setAgeTime(final Duration time)
  {
    assertMutable();
    if (time == null || time.isNegative()) {
      throw new IllegalArgumentException("Age time cannot be null or negative");
    }
    ageTime = time;
  }


  /**
   * Returns the prune priority.
   *
   * @return  priority value at which to prune
   */
  public long getPrunePriority()
  {
    return prunePriority;
  }


  /**
   * Sets the prune priority.
   *
   * @param  l  priority value at which to prune
   */
  public void setPrunePriority(final long l)
  {
    assertMutable();
    if (l < -1) {
      throw new IllegalArgumentException("Prune by priority must be greater than or equal to -1");
    }
    prunePriority = l;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "prunePeriod=" + getPrunePeriod() + ", " +
      "ageTime=" + ageTime + ", " +
      "prunePriority=" + prunePriority + "]";
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


  /** Age prune strategy builder. */
  public static class Builder extends
    AbstractPruneStrategy.AbstractBuilder<Builder, AgePruneStrategy>
  {


    /**
     * Creates a new builder.
     */
    protected Builder()
    {
      super(new AgePruneStrategy());
    }


    protected Builder(final AgePruneStrategy strategy)
    {
      super(strategy);
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the prune age time.
     *
     * @param  time  to set
     *
     * @return  this builder
     */
    public Builder age(final Duration time)
    {
      object.setAgeTime(time);
      return self();
    }


    /**
     * Sets the prune priority.
     *
     * @param  l  prune priority
     *
     * @return  this builder
     */
    public Builder priority(final long l)
    {
      object.setPrunePriority(l);
      return self();
    }
  }
}
