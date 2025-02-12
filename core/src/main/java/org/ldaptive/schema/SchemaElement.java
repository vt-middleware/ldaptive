/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Interface for schema elements.
 *
 * @param  <T>  type of element identifier key
 *
 * @author  Middleware Services
 */
public interface SchemaElement<T>
{


  /**
   * Returns the key for this element. Typically, an OID but may also be an integer.
   *
   * @return  element key
   */
  T getElementKey();


  /**
   * Returns this schema element as formatted string per RFC 4512.
   *
   * @return  formatted string
   */
  String format();
}
