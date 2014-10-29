/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PasswordPolicyAuthenticationResponseHandler
  implements AuthenticationResponseHandler
{


  /** {@inheritDoc} */
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
