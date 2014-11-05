/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.Request;

/**
 * Marker interface for ldap extended requests.
 *
 * @author  Middleware Services
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
