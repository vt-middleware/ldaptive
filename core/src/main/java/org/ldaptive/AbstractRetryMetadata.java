/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common implementation of retry metadata.
 *
 * @author  Middleware Services
 */
public abstract class AbstractRetryMetadata implements RetryMetadata
{

  /** Time at which the last success occurred. */
  protected Instant successTime;

  /** Time at which the failure occurred. */
  protected Instant failureTime;

  /** Attempt count. */
  private final AtomicInteger attempts = new AtomicInteger();


  @Override
  public Instant getSuccessTime()
  {
    return successTime;
  }


  @Override
  public Instant getFailureTime()
  {
    return failureTime;
  }


  @Override
  public int getAttempts()
  {
    return attempts.get();
  }


  @Override
  public void recordSuccess(final Instant time)
  {
    successTime = time;
  }


  @Override
  public void recordFailure(final Instant time)
  {
    failureTime = time;
    attempts.incrementAndGet();
  }


  @Override
  public String toString()
  {
    return new StringBuilder().append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("attempts=").append(attempts).append(", ")
      .append("failureTime=").append(failureTime).toString();
  }
}
