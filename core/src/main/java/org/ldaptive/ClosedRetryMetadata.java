/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;

/**
 * Retry metadata used when a connection is unexpectedly closed.
 *
 * @author  Middleware Services
 */
public class ClosedRetryMetadata extends AbstractRetryMetadata
{

  /** Last thrown exception. */
  protected final Throwable failureException;


  /**
   * Creates a new closed retry metadata.
   *
   * @param  time  of last successful connection
   * @param  ex  exception that caused the connection to close
   */
  public ClosedRetryMetadata(final Instant time, final Throwable ex)
  {
    successTime = time;
    failureException = ex;
  }


  /**
   * Returns the exception that caused the closed connection.
   *
   * @return failure exception
   */
  public Throwable getFailureException()
  {
    return failureException;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("failureException=").append(failureException).toString();
  }
}
