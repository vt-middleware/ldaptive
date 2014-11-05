/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.provider.Provider;

/**
 * Handles properties for {@link org.ldaptive.DefaultConnectionFactory}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DefaultConnectionFactoryPropertyInvoker
  extends AbstractPropertyInvoker
{


  /**
   * Creates a new default connection factory property invoker for the supplied
   * class.
   *
   * @param  c  class that has setter methods
   */
  public DefaultConnectionFactoryPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Provider.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(Provider.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
