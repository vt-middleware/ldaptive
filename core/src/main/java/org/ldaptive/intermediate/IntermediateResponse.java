/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.intermediate;

import org.ldaptive.ResponseMessage;
import org.ldaptive.asn1.DERBuffer;

/**
 * Interface for ldap intermediate responses.
 *
 * @author  Middleware Services
 */
public interface IntermediateResponse extends ResponseMessage
{


  /**
   * Returns the OID for this response.
   *
   * @return  oid
   */
  String getOID();


  /**
   * Initializes this response with the supplied BER encoded data.
   *
   * @param  encoded  BER encoded response value
   */
  void decode(DERBuffer encoded);
}
