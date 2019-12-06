/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;
import org.ldaptive.extended.IntermediateResponse;

/**
 * Marker interface for an intermediate response handler.
 *
 * @author  Middleware Services
 */
public interface IntermediateResponseHandler extends Consumer<IntermediateResponse> {}
