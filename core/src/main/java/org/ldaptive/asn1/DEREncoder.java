/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Interface for encoding DER objects.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface DEREncoder
{


  /**
   * Encode this object into it's DER type.
   *
   * @return  DER encoded object
   */
  byte[] encode();
}
