/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.control.RequestControl;

/**
 * Handles properties for {@link org.ldaptive.auth.SimpleBindAuthenticationHandler}.
 *
 * @author  Middleware Services
 */
public class SimpleBindAuthenticationHandlerPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new simple bind authentication handler property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public SimpleBindAuthenticationHandlerPropertyInvoker(final Class<?> c)
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
      } else if (RequestControl[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(RequestControl.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
