/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides an interface for defining connection validation.
 *
 * @author  Middleware Services
 */
public interface ConnectionValidator extends Function<Connection, Boolean>
{


  /**
   * Provides an asynchronous implementation of {@link #apply(Object)}. The supplied consumer will be invoked with the
   * validation result. {@link #getValidateTimeout()} must be enforced by the caller.
   *
   * @param  conn  to validate
   * @param  function  to consume the validation result
   */
  void applyAsync(Connection conn, Consumer<Boolean> function);


  /**
   * Provides an asynchronous implementation of {@link #apply(Object)}. The returned supplier will block until a
   * validation result is received respecting {@link #getValidateTimeout()}.
   *
   * @param  conn  to validate
   *
   * @return  supplier to retrieve the validation result
   */
  Supplier<Boolean> applyAsync(Connection conn);


  /**
   * Returns the interval at which the validation task will be executed.
   *
   * @return  validation period
   */
  Duration getValidatePeriod();


  /**
   * Returns the duration at which a validate operation should be abandoned.
   *
   * @return  validation timeout
   */
  Duration getValidateTimeout();
}
