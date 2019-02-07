/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.SearchFilter;
import org.ldaptive.control.RequestControl;

/**
 * Handles properties for {@link org.ldaptive.SearchRequest}.
 *
 * @author  Middleware Services
 */
public class SearchRequestPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new search request property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public SearchRequestPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (SearchFilter.class.isAssignableFrom(type)) {
        newValue = new SearchFilter(value);
      } else if (RequestControl[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(RequestControl.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
