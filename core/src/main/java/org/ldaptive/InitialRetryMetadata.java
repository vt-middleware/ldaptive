/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
   * @param  time  of last successful connection
   */
  public InitialRetryMetadata(final Instant time)
  {
    successTime = time;
  }
}
