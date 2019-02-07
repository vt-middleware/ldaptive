/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Consumer;

/**
 * Marker interface for a referral handler.
 *
 * @author  Middleware Services
 */
public interface ReferralHandler extends Consumer<String[]> {}
