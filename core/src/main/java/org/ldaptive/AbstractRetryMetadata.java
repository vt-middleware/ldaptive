/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Clock;
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

  /** Time at which this metadata was created. */
  private final Instant createTime;

  /** Attempt count. */
  private final AtomicInteger attempts = new AtomicInteger();


  /**
   * Creates a new abstract retry metadata.
   */
  public AbstractRetryMetadata()
  {
    this(Clock.systemDefaultZone());
  }


  /**
   * Creates a new abstract retry metadata.
   *
   * @param  clock  to set the create time
   */
  protected AbstractRetryMetadata(final Clock clock)
  {
    createTime = Instant.now(clock);
  }


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
  public Instant getCreateTime()
  {
    return createTime;
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
    return getClass().getName() + "@" + hashCode() + "::" +
      "attempts=" + attempts + ", " +
      "failureTime=" + failureTime;
  }
}
