/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;

/**
 * Contains properties related to retries.
 *
 * @author  Middleware Services
 */
public class RetryMetadata
{

  /** Time at which the failure occurred. */
  private final Instant failureTime;

  /** Attempt count. */
  private int attempts;


  /**
   * Creates a new retry metadata with failure time of {@link Instant#now()}.
   */
  public RetryMetadata()
  {
    failureTime = Instant.now();
  }


  /**
   * Creates a new retry metadata.
   *
   * @param  time  failure time
   */
  public RetryMetadata(final Instant time)
  {
    failureTime = time;
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
    return attempts;
  }


  /**
   * Increments the number of retry attempts made.
   */
  public void incrementAttempts()
  {
    attempts++;
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
