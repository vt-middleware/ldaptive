/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Interface for formatting schema elements.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public interface SchemaElementFormatter<T extends SchemaElement<?>>
{


  /**
   * Returns a string representation of the supplied schema element.
   *
   * @param  element  to format
   *
   * @return  formatted schema element
   */
  String format(T element);
}
