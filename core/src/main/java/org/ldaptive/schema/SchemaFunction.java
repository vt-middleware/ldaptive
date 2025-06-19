/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Marker interface for a schema function.
 *
 * @author  Middleware Services
 */
public interface SchemaFunction
{


  /**
   * Parses the supplied string representation of a schema element.
   *
   *
   * @param  <T>  type of schema element
   *
   * @param  type  class type of schema element
   * @param  definition  to parse
   *
   * @return parsed schema element
   *
   * @throws  SchemaParseException  if the supplied schema definition is invalid
   */
  <T extends SchemaElement<?>> T parse(Class<? extends T> type, String definition) throws SchemaParseException;
}
