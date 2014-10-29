/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive;

import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;

/**
 * Marker interface for all ldap requests.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
