/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Removes connections from the pool based on how long they have been idle in
 * the available queue. By default this implementation executes every 5 minutes
 * and prunes connections that have been idle for more than 10 minutes.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class IdlePruneStrategy implements PruneStrategy
{

  /** Default number of statistics to store. Value is {@value}. */
  private static final int DEFAULT_STATISTICS_SIZE = 1;

  /** Default prune period in seconds. Value is {@value}. */
  private static final long DEFAULT_PRUNE_PERIOD = 300;

  /** Default idle time in seconds. Value is {@value}. */
  private static final long DEFAULT_IDLE_TIME = 600;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Prune period in seconds. */
  private long prunePeriod;

  /** Idle time in seconds. */
  private long idleTime;


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
  public IdlePruneStrategy(final long period, final long idle)
  {
    prunePeriod = period;
    idleTime = idle;
  }


  /** {@inheritDoc} */
  @Override
  public boolean prune(final PooledConnectionProxy conn)
  {
    final long timeAvailable =
      conn.getPooledConnectionStatistics().getLastAvailableState();
    logger.trace(
      "evaluating timestamp {} for connection {}",
      timeAvailable,
      conn);
    return
      System.currentTimeMillis() - timeAvailable >
      TimeUnit.SECONDS.toMillis(idleTime);
  }


  /** {@inheritDoc} */
  @Override
  public int getStatisticsSize()
  {
    return DEFAULT_STATISTICS_SIZE;
  }


  /** {@inheritDoc} */
  @Override
  public long getPrunePeriod()
  {
    return prunePeriod;
  }


  /**
   * Sets the prune period.
   *
   * @param  period  to set
   */
  public void setPrunePeriod(final long period)
  {
    prunePeriod = period;
  }


  /**
   * Returns the idle time.
   *
   * @return  idle time
   */
  public long getIdleTime()
  {
    return idleTime;
  }


  /**
   * Sets the idle time.
   *
   * @param  time  that a connection has been idle and should be pruned
   */
  public void setIdleTime(final long time)
  {
    idleTime = time;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::prunePeriod=%s, idleTime=%s]",
        getClass().getName(),
        hashCode(),
        prunePeriod,
        idleTime);
  }
}
