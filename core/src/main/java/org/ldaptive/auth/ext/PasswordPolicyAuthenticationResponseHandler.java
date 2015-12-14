/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Attempts to parse the authentication response message and set the account state using data associated with a password
 * policy control.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  @Override
  public void handle(final AuthenticationResponse response)
  {
    final PasswordPolicyControl ppc = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    if (ppc != null) {
      if (ppc.getError() != null) {
        response.setAccountState(new PasswordPolicyAccountState(ppc.getError()));
      } else {
        ZonedDateTime exp = null;
        if (ppc.getTimeBeforeExpiration() > 0) {
          exp = ZonedDateTime.now().plusSeconds(ppc.getTimeBeforeExpiration());
        }
        if (exp != null || ppc.getGraceAuthNsRemaining() > 0) {
          response.setAccountState(new PasswordPolicyAccountState(exp, ppc.getGraceAuthNsRemaining()));
        }
      }
    }
  }
}
