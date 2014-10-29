/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.props;

import org.ldaptive.SearchFilter;
import org.ldaptive.control.RequestControl;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.handler.SearchReferenceHandler;

/**
 * Handles properties for {@link org.ldaptive.SearchRequest}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (SearchFilter.class.isAssignableFrom(type)) {
        newValue = new SearchFilter(value);
      } else if (RequestControl[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          RequestControl.class,
          value);
      } else if (SearchEntryHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          SearchEntryHandler.class,
          value);
      } else if (SearchReferenceHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          SearchReferenceHandler.class,
          value);
      } else if (IntermediateResponseHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          IntermediateResponseHandler.class,
          value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
