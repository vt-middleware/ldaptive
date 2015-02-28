/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.referral.ReferralHandler;

/**
 * Marker interface for all ldap requests.
 *
 * @author  Middleware Services
 */
public interface Request extends Message<RequestControl>
{


  /**
   * Returns the referral handler.
   *
   * @return  referral handler
   */
  ReferralHandler getReferralHandler();


  /**
   * Returns the intermediate response handlers.
   *
   * @return  intermediate response handlers
   */
  IntermediateResponseHandler[] getIntermediateResponseHandlers();
}
