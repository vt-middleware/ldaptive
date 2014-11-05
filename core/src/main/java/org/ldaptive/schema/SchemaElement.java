/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Interface for schema elements.
 *
 * @author  Middleware Services
 * @version  $Revision: 2894 $ $Date: 2014-03-07 10:56:39 -0500 (Fri, 07 Mar 2014) $
 */
public interface SchemaElement
{


  /**
   * Returns this schema element as formatted string per RFC 4512.
   *
   * @return  formatted string
   */
  String format();
}
