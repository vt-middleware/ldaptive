/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.ad.extended.FastBindRequest;
import org.ldaptive.ad.extended.FastBindResponse;
import org.ldaptive.asn1.DERBuffer;

/**
 * Utility class for creating extended responses.
 *
 * @author  Middleware Services
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
    final DERBuffer encoded)
  {
    final ExtendedResponse<?> res;
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
