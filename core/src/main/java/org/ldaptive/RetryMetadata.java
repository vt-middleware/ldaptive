/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;

/**
 * Contains properties related to retries.
 *
 * @author  Middleware Services
 */
public interface RetryMetadata
{


  /**
   * Returns the success time.
   *
   * @return  time that the success occurred
   */
  Instant getSuccessTime();


  /**
   * Returns the failure time.
   *
   * @return  time that the failure occurred
   */
  Instant getFailureTime();


  /**
   * Number of attempts for this retry.
   *
   * @return  retry attempts
   */
  int getAttempts();


  /**
   * Records a connection success at the given instant.
   *
   * @param  time  Point in time where connection was opened.
   */
  void recordSuccess(Instant time);


  /**
   * Records a connection failure at the given instant.
   *
   * @param  time  Point in time where connection failed.
   */
  void recordFailure(Instant time);
}
