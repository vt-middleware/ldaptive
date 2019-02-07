/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;

/**
 * Marker interface for a compare result handler.
 *
 * @author  Middleware Services
 */
public interface CompareValueHandler extends Consumer<Boolean> {}
