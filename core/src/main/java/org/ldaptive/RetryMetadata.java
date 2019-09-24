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

  /** Time at which the last success occurred. */
  private Instant successTime;

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
   * Creates a new retry metadata.
   *
   * @param  strategy  connection strategy
   * @param  time  time of last successful connection
   */
  public RetryMetadata(final ConnectionStrategy strategy, final Instant time)
  {
    connectionStrategy = strategy;
    successTime = time;
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
   * Returns the success time.
   *
   * @return  time that the success occurred
   */
  public Instant getSuccessTime()
  {
    return successTime;
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
   * Records a connection success at the given instant.
   *
   * @param  time  Point in time where connection was opened.
   */
  public void recordSuccess(final Instant time)
  {
    successTime = time;
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


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("attempts=").append(attempts).append(", ")
      .append("failureTime=").append(failureTime).append("]").toString();
  }
}
