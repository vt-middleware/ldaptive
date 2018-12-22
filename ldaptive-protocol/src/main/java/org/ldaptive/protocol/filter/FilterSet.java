/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;


import org.ldaptive.protocol.SearchFilter;

/**
 * Container of search filters.
 *
 * @author  Middleware Services
 */
public interface FilterSet extends SearchFilter
{


  /**
   * Returns the type of filter set.
   *
   * @return  type of filter set
   */
  Type getType();


  /**
   * Adds a search filter to this set.
   *
   * @param  filter  to add
   */
  void add(SearchFilter filter);
}
