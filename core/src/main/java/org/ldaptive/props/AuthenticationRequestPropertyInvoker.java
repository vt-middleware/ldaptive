/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.Credential;

/**
 * Handles properties for {@link org.ldaptive.auth.AuthenticationRequest}.
 *
 * @author  Middleware Services
 */
public class AuthenticationRequestPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new authentication request property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public AuthenticationRequestPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (Credential.class.isAssignableFrom(type)) {
        newValue = new Credential(value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
