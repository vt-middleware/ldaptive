/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.auth.AuthenticationHandler;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.DnResolver;
import org.ldaptive.auth.EntryResolver;

/**
 * Handles properties for {@link org.ldaptive.auth.Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorPropertyInvoker extends AbstractPropertyInvoker
{


  /**
   * Creates a new authenticator property invoker for the supplied class.
   *
   * @param  c  class that has setter methods
   */
  public AuthenticatorPropertyInvoker(final Class<?> c)
  {
    initialize(c);
  }


  @Override
  protected Object convertValue(final Class<?> type, final String value)
  {
    Object newValue = value;
    if (type != String.class) {
      if (DnResolver.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(DnResolver.class, value);
      } else if (AuthenticationHandler.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(
          AuthenticationHandler.class,
          value);
      } else if (AuthenticationResponseHandler[].class.isAssignableFrom(type)) {
        newValue = createArrayTypeFromPropertyValue(
          AuthenticationResponseHandler.class,
          value);
      } else if (EntryResolver.class.isAssignableFrom(type)) {
        newValue = createTypeFromPropertyValue(EntryResolver.class, value);
      } else {
        newValue = convertSimpleType(type, value);
      }
    }
    return newValue;
  }
}
