/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.provider.Provider;

/**
 * Handles properties for {@link org.ldaptive.PooledConnectionFactory}.
 *
 * @author  Middleware Services
 */
public class PooledConnectionFactoryPropertyInvoker extends BlockingConnectionPoolPropertyInvoker
{


  /**
   * Creates a new pooled connection factory property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public PooledConnectionFactoryPropertyInvoker(final Class<?> c)
  {
    super(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Provider.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(Provider.class, value);
      } else {
        newValue = super.convertValue(type, value);
      }
    }
    return newValue;
  }
}
