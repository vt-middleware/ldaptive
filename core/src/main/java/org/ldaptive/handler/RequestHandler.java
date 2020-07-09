/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Function;
import org.ldaptive.Request;

/**
 * Marker interface for a request handler.
 *
 * @param  <Q>  type of request
 *
 * @author  Middleware Services
 */
public interface RequestHandler<Q extends Request> extends Function<Q, Q> {}
