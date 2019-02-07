/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.BiConsumer;

/**
 * Marker interface for an extended result handler.
 *
 * @author  Middleware Services
 */
public interface ExtendedValueHandler extends BiConsumer<String, byte[]> {}
