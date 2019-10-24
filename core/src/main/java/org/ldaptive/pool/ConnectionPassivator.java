/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import java.util.function.Function;
import org.ldaptive.Connection;

/**
 * Provides an interface for passivating connections when they are checked back into the pool.
 *
 * @author  Middleware Services
 */
public interface ConnectionPassivator extends Function<Connection, Boolean> {}
