/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Clock;
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
   * @param  clock  to set the create time
   * @param  time  of last successful connection
   * @param  ex  exception that caused the connection to close
   */
  ClosedRetryMetadata(final Clock clock, final Instant time, final Throwable ex)
  {
    super(clock);
    successTime = time;
    failureException = ex;
  }


  /**
   * Creates a new closed retry metadata.
   *
   * @param  time  of last successful connection
   * @param  ex  exception that caused the connection to close
   */
  public ClosedRetryMetadata(final Instant time, final Throwable ex)
  {
    this(Clock.systemDefaultZone(), time, ex);
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
    return super.toString() + ", " + "failureException=" + failureException;
  }
}
