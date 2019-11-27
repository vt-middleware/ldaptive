/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

/**
 * Marker interface for a filter function.
 *
 * @author  Middleware Services
 */
@FunctionalInterface
public interface FilterFunction
{


  /**
   * Parses the supplied string representation of a filter.
   *
   * @param  filter  to parse
   *
   * @return  parsed filter
   *
   * @throws  FilterParseException  if the supplied filter is invalid
   */
  Filter parse(String filter) throws FilterParseException;
}
