/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

/**
 * Encapsulates a {@link FilterFunction} and exposes a convenience static method for parsing filters..
 *
 * @author  Middleware Services
 */
public final class FilterParser
{

  /** Default filter function. */
  private static final FilterFunction FILTER_FUNCTION = new DefaultFilterFunction();


  /** Default constructor. */
  private FilterParser() {}


  /**
   * Parse the supplied filter string.
   *
   * @param  filter  to parse
   *
   * @return  search filter
   *
   * @throws  FilterParseException  if filter is invalid
   */
  public static Filter parse(final String filter)
    throws FilterParseException
  {
    return FILTER_FUNCTION.parse(filter);
  }
}
