/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;
import org.ldaptive.Result;

/**
 * Marker interface for a result handler.
 *
 * @author  Middleware Services
 */
public interface ResultHandler extends Consumer<Result> {}
