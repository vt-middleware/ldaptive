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
package org.ldaptive.intermediate;

import org.ldaptive.control.ResponseControl;

/**
 * Utility class for creating intermediate responses.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class IntermediateResponseFactory
{


  /** Default constructor. */
  private IntermediateResponseFactory() {}


  /**
   * Creates an intermediate response from the supplied response data.
   *
   * @param  oid  of the response
   * @param  encoded  BER encoding of the response
   * @param  responseControls  associated with this response
   * @param  msgId  message id associated with this response
   *
   * @return  intermediate response
   */
  public static IntermediateResponse createIntermediateResponse(
    final String oid,
    final byte[] encoded,
    final ResponseControl[] responseControls,
    final int msgId)
  {
    IntermediateResponse res;
    if (SyncInfoMessage.OID.equals(oid)) {
      res = new SyncInfoMessage(responseControls, msgId);
      res.decode(encoded);
    } else {
      throw new IllegalArgumentException("Unknown OID: " + oid);
    }
    return res;
  }
}
