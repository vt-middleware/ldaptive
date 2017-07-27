/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import org.ldaptive.auth.AddControlAuthenticationRequestHandler;
import org.ldaptive.auth.User;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.RequestControl;

/**
 * Adds the {@link PasswordPolicyControl} to the {@link org.ldaptive.auth.AuthenticationRequest}.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAuthenticationRequestHandler extends AddControlAuthenticationRequestHandler
{


  /**
   * Creates a new password policy authentication request handler
   */
  public PasswordPolicyAuthenticationRequestHandler()
  {
    super(new ControlFactory()
    {


      @Override
      public RequestControl[] getControls(final String dn, final User user)
      {
        return new RequestControl[] {new PasswordPolicyControl()};
      }
    });
  }
}
