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

import org.ldaptive.ad.extended.FastBindRequest;
import org.ldaptive.ad.extended.FastBindResponse;

/**
 * Utility class for creating extended responses.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class ExtendedResponseFactory
{


  /** Default constructor. */
  private ExtendedResponseFactory() {}


  /**
   * Creates an extended response from the supplied response data.
   *
   * @param  requestOID  of the extended request
   * @param  responseOID  of the extended response
   * @param  encoded  BER encoding of the extended response
   *
   * @return  extended response
   */
  public static ExtendedResponse<?> createExtendedResponse(
    final String requestOID,
    final String responseOID,
    final byte[] encoded)
  {
    ExtendedResponse<?> res;
    if (PasswordModifyRequest.OID.equals(requestOID)) {
      res = new PasswordModifyResponse();
      if (encoded != null) {
        res.decode(encoded);
      }
    } else if (WhoAmIRequest.OID.equals(requestOID)) {
      res = new WhoAmIResponse();
      if (encoded != null) {
        res.decode(encoded);
      }
    } else if (CancelRequest.OID.equals(requestOID)) {
      res = new CancelResponse();
    } else if (FastBindRequest.OID.equals(requestOID)) {
      res = new FastBindResponse();
    } else {
      throw new IllegalArgumentException("Unknown OID: " + responseOID);
    }
    return res;
  }
}
