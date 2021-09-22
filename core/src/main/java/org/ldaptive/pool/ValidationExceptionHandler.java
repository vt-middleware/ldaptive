/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.function.Function;
import org.ldaptive.Connection;

/**
 * Marker interface for a validation exception handler.
 *
 * @author  Middleware Services
 */
public interface ValidationExceptionHandler extends Function<ValidationException, Connection> {}
