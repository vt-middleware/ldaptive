/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Attempts to parse the authentication response message and set the account
 * state using data associated with a password policy control.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAuthenticationResponseHandler
  implements AuthenticationResponseHandler
{


  @Override
  public void handle(final AuthenticationResponse response)
  {
    final PasswordPolicyControl ppc = (PasswordPolicyControl)
      response.getControl(PasswordPolicyControl.OID);
    if (ppc != null) {
      Calendar exp = null;
      if (ppc.getTimeBeforeExpiration() > 0) {
        exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, ppc.getTimeBeforeExpiration());
      }
      if (exp != null || ppc.getGraceAuthNsRemaining() > 0) {
        response.setAccountState(
          new PasswordPolicyAccountState(exp, ppc.getGraceAuthNsRemaining()));
      }
      if (response.getAccountState() == null && ppc.getError() != null) {
        response.setAccountState(
          new PasswordPolicyAccountState(ppc.getError()));
      }
    }
  }
}
