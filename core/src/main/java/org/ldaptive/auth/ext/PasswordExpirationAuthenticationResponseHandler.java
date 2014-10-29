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
import org.ldaptive.control.PasswordExpiredControl;
import org.ldaptive.control.PasswordExpiringControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to parse the authentication response and set the account state using
 * data associated with the password expiring and password expired controls. See
 * http://tools.ietf.org/html/draft-vchu-ldap-pwd-policy-00.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PasswordExpirationAuthenticationResponseHandler
  implements AuthenticationResponseHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  /** {@inheritDoc} */
  @Override
  public void handle(final AuthenticationResponse response)
  {
    final PasswordExpiringControl expiringControl = (PasswordExpiringControl)
      response.getControl(PasswordExpiringControl.OID);
    if (expiringControl != null) {
      if (expiringControl.getTimeBeforeExpiration() > 0) {
        final Calendar exp = Calendar.getInstance();
        exp.add(Calendar.SECOND, expiringControl.getTimeBeforeExpiration());
        response.setAccountState(new PasswordExpirationAccountState(exp));
      } else {
        logger.warn(
          "Received password expiring control with non-positive value: %s",
          expiringControl);
      }
    }

    if (response.getAccountState() == null) {
      final PasswordExpiredControl expiredControl = (PasswordExpiredControl)
        response.getControl(PasswordExpiredControl.OID);
      if (expiredControl != null) {
        response.setAccountState(
          new PasswordExpirationAccountState(
            PasswordExpirationAccountState.Error.PASSWORD_EXPIRED));
      }
    }
  }
}
