/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.pool.Activator;
import org.ldaptive.pool.Passivator;
import org.ldaptive.pool.PruneStrategy;
import org.ldaptive.pool.Validator;

/**
 * Handles properties for {@link org.ldaptive.pool.BlockingConnectionPool}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BlockingConnectionPoolPropertyInvoker
  extends AbstractPropertyInvoker
{


  /**
   * Creates a new blocking connection pool property invoker for the supplied
   * class.
   *
   * @param  c  class that has setter methods
   */
  public BlockingConnectionPoolPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  /** {@inheritDoc} */
  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Activator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(Activator.class, value);
      } else if (Passivator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(Passivator.class, value);
      } else if (PruneStrategy.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(PruneStrategy.class, value);
      } else if (Validator.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(Validator.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
