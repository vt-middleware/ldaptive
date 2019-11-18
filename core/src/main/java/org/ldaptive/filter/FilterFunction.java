/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import java.util.function.Function;

/**
 * Marker interface for a filter function.
 *
 * @author  Middleware Services
 */
public interface FilterFunction extends Function<String, Filter> {}
