/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Function;
import org.ldaptive.Result;

/**
 * Marker interface for a referral result handler.
 *
 * @param  <S>  type of response
 *
 * @author  Middleware Services
 */
public interface ReferralResultHandler<S extends Result> extends Function<S, S> {}
