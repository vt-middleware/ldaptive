/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.function.Function;
import org.ldaptive.Connection;

/**
 * Provides an interface for activating connections when they are checked out from the pool.
 *
 * @author  Middleware Services
 */
public interface ConnectionActivator extends Function<Connection, Boolean> {}
