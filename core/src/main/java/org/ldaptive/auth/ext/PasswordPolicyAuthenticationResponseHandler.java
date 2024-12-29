/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Clock;
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

  /** Clock to convert time before expiration seconds to a datetime. */
  private final Clock expirationClock;


  /**
   * Creates a new password policy authentication response handler.
   */
  public PasswordPolicyAuthenticationResponseHandler()
  {
    this(Clock.systemDefaultZone());
  }


  /**
   * Creates a new password policy authentication response handler.
   *
   * @param  clock  used to convert time before expiration to a datetime
   */
  PasswordPolicyAuthenticationResponseHandler(final Clock clock)
  {
    expirationClock = clock;
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    final PasswordPolicyControl ppc = (PasswordPolicyControl) response.getControl(PasswordPolicyControl.OID);
    if (ppc != null) {
      final ZonedDateTime exp = getTimeBeforeExpiration(ppc);
      if (exp != null) {
        if (ppc.hasError()) {
          response.setAccountState(new PasswordPolicyAccountState(exp, ppc.getError()));
        } else {
          response.setAccountState(new PasswordPolicyAccountState(exp));
        }
      } else if (ppc.hasWarning(PasswordPolicyControl.WarningType.GRACE_AUTHNS_REMAINING)) {
        final int graceAuthnsRemaining = ppc.getWarning().getValue();
        if (ppc.hasError()) {
          response.setAccountState(new PasswordPolicyAccountState(graceAuthnsRemaining, ppc.getError()));
        } else {
          response.setAccountState(new PasswordPolicyAccountState(graceAuthnsRemaining));
        }
      } else if (ppc.hasError()) {
        response.setAccountState(new PasswordPolicyAccountState(ppc.getError()));
      }
    }
  }


  /**
   * Returns a zoned date time for the time before expiration on the supplied control.
   *
   * @param  ppc  to inspect
   *
   * @return  date time or null
   */
  private ZonedDateTime getTimeBeforeExpiration(final PasswordPolicyControl ppc)
  {
    if (ppc.hasWarning(PasswordPolicyControl.WarningType.TIME_BEFORE_EXPIRATION)) {
      return ZonedDateTime.now(expirationClock).plusSeconds(ppc.getWarning().getValue());
    }
    return null;
  }
}
