/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Function;

/**
 * Provides an interface for defining connection validation.
 *
 * @author  Middleware Services
 */
public interface ConnectionValidator extends Function<Connection, Boolean>
{


  /**
   * Returns the interval at which the validation task will be executed.
   *
   * @return  validation period
   */
  Duration getValidatePeriod();


  /**
   * Returns the time at which a validate operation should be abandoned.
   *
   * @return  validation timeout
   */
  Duration getValidateTimeout();
}
