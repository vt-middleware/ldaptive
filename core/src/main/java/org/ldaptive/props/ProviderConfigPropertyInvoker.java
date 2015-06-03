/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

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


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ControlProcessor.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ControlProcessor.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
