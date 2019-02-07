/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Handles properties for implementations of {@link org.ldaptive.ConnectionFactoryManager}.
 *
 * @author  Middleware Services
 */
public class SearchOperationFactoryPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new search dn resolver property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public SearchOperationFactoryPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ConnectionFactory.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ConnectionFactory.class, value);
      } else if (ExceptionHandler.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ExceptionHandler.class, value);
      } else if (LdapEntryHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(LdapEntryHandler.class, value);
      } else if (ResultHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(ResultHandler.class, value);
      } else if (SearchResultHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(SearchResultHandler.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
