/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

/**
 * Marker interface for a schema definition function.
 *
 * @param  <T>  type of schema element
 *
 * @author  Middleware Services
 */
public interface DefinitionFunction<T extends SchemaElement<?>>
{


  /**
   * Parses the supplied string representation of a schema element.
   *
   * @param  definition  to parse
   *
   * @return  parsed schema element
   *
   * @throws  SchemaParseException  if the supplied schema definition is invalid
   */
  T parse(String definition) throws SchemaParseException;
}
