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
package org.ldaptive.provider;

import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;

/**
 * Handles provider specific request and response controls.
 *
 * @param  <T>  type of provider control
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ControlHandler<T>
{


  /**
   * Returns the OID of the supplied control.
   *
   * @param  control  to return oid for
   *
   * @return  control oid
   */
  String getOID(T control);


  /**
   * Converts the supplied control to a provider specific request control.
   *
   * @param  requestControl  to convert
   *
   * @return  provider specific controls
   */
  T handleRequest(RequestControl requestControl);


  /**
   * Converts the supplied provider control to a response control.
   *
   * @param  responseControl  to convert
   *
   * @return  control
   */
  ResponseControl handleResponse(T responseControl);
}
