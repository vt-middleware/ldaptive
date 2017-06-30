/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import org.ldaptive.auth.AddControlAuthenticationRequestHandler;
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
    super((dn, user) -> new RequestControl[] {new PasswordPolicyControl()});
  }
}
