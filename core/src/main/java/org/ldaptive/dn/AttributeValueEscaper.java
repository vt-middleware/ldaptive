/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

/**
 * Interface for escaping attribute values.
 *
 * @author  Middleware Services
 */
public interface AttributeValueEscaper
{


  /**
   * Escapes the supplied attribute value.
   *
   * @param  value  to escape
   *
   * @return  escaped value
   */
  String escape(String value);
}
