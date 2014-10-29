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
package org.ldaptive.extended;

import org.ldaptive.Request;

/**
 * Marker interface for ldap extended requests.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ExtendedRequest extends Request
{


  /**
   * Returns the OID for this extended request.
   *
   * @return  oid
   */
  String getOID();


  /**
   * Provides the BER encoding of this request.
   *
   * @return  BER encoded request
   */
  byte[] encode();
}
