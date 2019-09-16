/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains properties related to retries.
 *
 * @author  Middleware Services
 */
public class RetryMetadata
{

  /** Attempt count. */
  private final AtomicInteger attempts = new AtomicInteger();

  /** Connection strategy associated with this retry. */
  private final ConnectionStrategy connectionStrategy;

  /** Time at which the failure occurred. */
  private Instant failureTime;


  /**
   * Creates a new retry metadata.
   *
   * @param  strategy  connection strategy
   */
  public RetryMetadata(final ConnectionStrategy strategy)
  {
    connectionStrategy = strategy;
  }


  /**
   * Return the connection strategy.
   *
   * @return  connection strategy
   */
  public ConnectionStrategy getConnectionStrategy()
  {
    return connectionStrategy;
  }


  /**
   * Returns the failure time.
   *
   * @return  time that the failure occurred
   */
  public Instant getFailureTime()
  {
    return failureTime;
  }


  /**
   * Number of attempts for this retry.
   *
   * @return  retry attempts
   */
  public int getAttempts()
  {
    return attempts.get();
  }


  /**
   * Records a connection failure at the given instant.
   *
   * @param  time  Point in time where connection failed.
   */
  public void recordFailure(final Instant time)
  {
    failureTime = time;
    attempts.incrementAndGet();
  }


  /**
   * Whether this retry metadata has recorded a failure.
   *
   * @return  whether at least one connection attempt has failed, false otherwise.
   */
  public boolean hasFailed()
  {
    return attempts.get() == 0;
  }


  /**
   * Resets the internal failure tracking state such that {@link #hasFailed()} returns false until a failure is
   * subsequently recorded.
   */
  public void reset()
  {
    failureTime = null;
    attempts.set(0);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("attempts=").append(attempts).append(", ")
      .append("failureTime=").append(failureTime).append("]").toString();
  }
}
