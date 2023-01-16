/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Interface for encoding DER objects.
 *
 * @author  Middleware Services
 */
public interface DEREncoder
{


  /**
   * Encode this object into its DER type.
   *
   * @return  DER encoded object
   */
  byte[] encode();
}
