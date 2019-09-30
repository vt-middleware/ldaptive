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


  /**
   * Creates a new closed retry metadata.
   *
   * @param  time  of last successful connection
   */
  public ClosedRetryMetadata(final Instant time)
  {
    successTime = time;
  }
}
