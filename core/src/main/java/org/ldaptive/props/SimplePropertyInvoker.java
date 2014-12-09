/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

/**
 * Handles simple properties common to all objects.
 *
 * @author  Middleware Services
 */
public class SimplePropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new simple property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public SimplePropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    return convertSimpleType(type, value);
  }
}
