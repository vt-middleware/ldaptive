/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

/**
 * Interface for normalizing RDNs.
 *
 * @author  Middleware Services
 */
public interface RDnNormalizer
{


  /**
   * Normalize the name value pairs in the supplied RDN.
   *
   * @param  rdn  to normalize
   *
   * @return  new normalized RDN
   */
  RDn normalize(RDn rdn);
}
