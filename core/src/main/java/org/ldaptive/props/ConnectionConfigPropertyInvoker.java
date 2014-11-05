/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ConnectionInitializer;

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


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ConnectionInitializer.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(
          ConnectionInitializer.class,
          value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
