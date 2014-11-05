/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;

/**
 * Marker interface for all ldap requests.
 *
 * @author  Middleware Services
 */
public interface Request extends Message<RequestControl>
{


  /**
   * Returns whether to follow referrals.
   *
   * @return  whether to follow referrals
   */
  boolean getFollowReferrals();


  /**
   * Returns the intermediate response handlers.
   *
   * @return  intermediate response handlers
   */
  IntermediateResponseHandler[] getIntermediateResponseHandlers();
}
