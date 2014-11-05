/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.provider.ConnectionStrategy;
import org.ldaptive.provider.ControlProcessor;

/**
 * Handles properties for {@link org.ldaptive.provider.ProviderConfig}.
 *
 * @author  Middleware Services
 */
public class ProviderConfigPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new provider config property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public ProviderConfigPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ConnectionStrategy.class.isAssignableFrom(type)) {
        if ("DEFAULT".equals(value)) {
          newValue = ConnectionStrategy.DEFAULT;
        } else if ("ACTIVE_PASSIVE".equals(value)) {
          newValue = ConnectionStrategy.ACTIVE_PASSIVE;
        } else if ("ROUND_ROBIN".equals(value)) {
          newValue = ConnectionStrategy.ROUND_ROBIN;
        } else if ("RANDOM".equals(value)) {
          newValue = ConnectionStrategy.RANDOM;
        } else {
          newValue = createTypeFromPropertyValue(
            ConnectionStrategy.class,
            value);
        }
      } else if (ControlProcessor.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ControlProcessor.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
