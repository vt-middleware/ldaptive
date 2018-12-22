/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

/**
 * LDAP protocol request.
 *
 * @author  Middleware Services
 */
public interface Request
{


  /**
   * Encode this request as asn.1.
   *
   * @param  id  message id of this request
   *
   * @return  asn.1 encoded request
   */
  byte[] encode(int id);
}
