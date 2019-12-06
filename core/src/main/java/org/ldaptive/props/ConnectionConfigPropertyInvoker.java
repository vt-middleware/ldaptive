/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ActivePassiveConnectionStrategy;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.DnsSrvConnectionStrategy;
import org.ldaptive.RandomConnectionStrategy;
import org.ldaptive.RoundRobinConnectionStrategy;

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
      if (ConnectionInitializer[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(ConnectionInitializer.class, value);
      } else if (ConnectionStrategy.class.isAssignableFrom(type)) {
        if ("ACTIVE_PASSIVE".equals(value)) {
          newValue = new ActivePassiveConnectionStrategy();
        } else if ("ROUND_ROBIN".equals(value)) {
          newValue = new RoundRobinConnectionStrategy();
        } else if ("RANDOM".equals(value)) {
          newValue = new RandomConnectionStrategy();
        } else if ("DNS_SRV".equals(value)) {
          newValue = new DnsSrvConnectionStrategy();
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
