/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

import java.util.List;

/**
 * Interface for parsing DNs.
 *
 * @author  Middleware Services
 */
public interface DnParser
{

  /**
   * Parses the supplied DN into a list of RDNs.
   *
   * @param  dn  to parse
   *
   * @return  list of RDNs
   */
  List<RDn> parse(String dn);
}
