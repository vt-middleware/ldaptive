/* See LICENSE for licensing and NOTICE for copyright. */
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
    switch (requestOID) {
    case PasswordModifyRequest.OID:
      res = new PasswordModifyResponse();
      if (encoded != null) {
        res.decode(encoded);
      }
      break;
    case WhoAmIRequest.OID:
      res = new WhoAmIResponse();
      if (encoded != null) {
        res.decode(encoded);
      }
      break;
    case CancelRequest.OID:
      res = new CancelResponse();
      break;
    case FastBindRequest.OID:
      res = new FastBindResponse();
      break;
    default:
      throw new IllegalArgumentException("Unknown OID: " + responseOID);
    }
    return res;
  }
}
