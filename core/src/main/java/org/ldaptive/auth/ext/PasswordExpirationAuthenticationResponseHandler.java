/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.control.PasswordExpiredControl;
import org.ldaptive.control.PasswordExpiringControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to parse the authentication response and set the account state using data associated with the password
 * expiring and password expired controls. See http://tools.ietf.org/html/draft-vchu-ldap-pwd-policy-00.
 *
 * @author  Middleware Services
 */
public class PasswordExpirationAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  @Override
  public void handle(final AuthenticationResponse response)
  {
    final PasswordExpiringControl expiringControl = (PasswordExpiringControl) response.getControl(
      PasswordExpiringControl.OID);
    if (expiringControl != null) {
      if (expiringControl.getTimeBeforeExpiration() > 0) {
        final ZonedDateTime exp = ZonedDateTime.now().plusSeconds(expiringControl.getTimeBeforeExpiration());
        response.setAccountState(new PasswordExpirationAccountState(exp));
      } else {
        logger.warn("Received password expiring control with non-positive value: {}", expiringControl);
      }
    }

    if (response.getAccountState() == null) {
      final PasswordExpiredControl expiredControl = (PasswordExpiredControl) response.getControl(
        PasswordExpiredControl.OID);
      if (expiredControl != null) {
        response.setAccountState(
          new PasswordExpirationAccountState(PasswordExpirationAccountState.Error.PASSWORD_EXPIRED));
      }
    }
  }
}
