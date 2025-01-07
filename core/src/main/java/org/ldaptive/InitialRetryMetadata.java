/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Clock;
import java.time.Instant;

/**
 * Retry metadata used when a connection is opened.
 *
 * @author  Middleware Services
 */
public class InitialRetryMetadata extends AbstractRetryMetadata
{


  /**
   * Creates a new initial retry metadata.
   *
   * @param  clock  to set the create time
   * @param  time  of last successful connection
   */
  InitialRetryMetadata(final Clock clock, final Instant time)
  {
    super(clock);
    successTime = time;
  }


  /**
   * Creates a new initial retry metadata.
   *
   * @param  time  of last successful connection
   */
  public InitialRetryMetadata(final Instant time)
  {
    this(Clock.systemDefaultZone(), time);
  }
}
