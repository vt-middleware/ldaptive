/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Interface for schema elements.
 *
 * @author  Middleware Services
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
