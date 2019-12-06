/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ConnectionValidator;
import org.ldaptive.pool.ConnectionActivator;
import org.ldaptive.pool.ConnectionPassivator;
import org.ldaptive.pool.PruneStrategy;

/**
 * Handles properties for {@link org.ldaptive.pool.BlockingConnectionPool}.
 *
 * @author  Middleware Services
 */
public class BlockingConnectionPoolPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new blocking connection pool property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public BlockingConnectionPoolPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (ConnectionActivator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ConnectionActivator.class, value);
      } else if (ConnectionPassivator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ConnectionPassivator.class, value);
      } else if (ConnectionValidator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(ConnectionValidator.class, value);
      } else if (PruneStrategy.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(PruneStrategy.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
