/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ConnectionInitializer;
import org.ldaptive.ConnectionStrategy;

/**
 * Handles properties for {@link org.ldaptive.ConnectionConfig}.
 *
 * @author  Middleware Services
 */
public class ConnectionConfigPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new connection config property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public ConnectionConfigPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ConnectionInitializer.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ConnectionInitializer.class, value);
      } else if (ConnectionStrategy.class.isAssignableFrom(type)) {
        if ("DEFAULT".equals(value)) {
          newValue = ConnectionStrategy.DEFAULT;
        } else if ("ACTIVE_PASSIVE".equals(value)) {
          newValue = ConnectionStrategy.ACTIVE_PASSIVE;
        } else if ("ROUND_ROBIN".equals(value)) {
          newValue = ConnectionStrategy.ROUND_ROBIN;
        } else if ("RANDOM".equals(value)) {
          newValue = ConnectionStrategy.RANDOM;
        } else {
          newValue = createTypeFromPropertyValue(ConnectionStrategy.class, value);
        }
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
