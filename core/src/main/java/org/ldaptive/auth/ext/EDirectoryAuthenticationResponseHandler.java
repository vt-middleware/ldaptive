/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;

/**
 * Attempts to parse the authentication response and set the account state using data associated with eDirectory. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'passwordExpirationTime' and
 * 'loginGraceRemaining' attributes so they can be consumed by this handler.
 *
 * @author  Middleware Services
 */
public class EDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Number of hours before expiration to produce a warning. */
  private int warningHours;


  /** Default constructor. */
  public EDirectoryAuthenticationResponseHandler() {}


  /**
   * Creates a new edirectory authentication response handler.
   *
   * @param  hours  length of time before expiration that should produce a warning
   */
  public EDirectoryAuthenticationResponseHandler(final int hours)
  {
    if (hours <= 0) {
      throw new IllegalArgumentException("Hours must be > 0");
    }
    warningHours = hours;
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getMessage() != null) {
      final EDirectoryAccountState.Error edError = EDirectoryAccountState.Error.parse(response.getMessage());
      if (edError != null) {
        response.setAccountState(new EDirectoryAccountState(edError));
      }
    } else if (response.getResult()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("passwordExpirationTime");
      final LdapAttribute loginRemaining = entry.getAttribute("loginGraceRemaining");
      final int loginRemainingValue = loginRemaining != null ? Integer.parseInt(loginRemaining.getStringValue()) : 0;

      if (expTime != null) {
        final Calendar exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
        if (warningHours > 0) {
          final Calendar now = Calendar.getInstance();
          final Calendar warn = (Calendar) exp.clone();
          warn.add(Calendar.HOUR_OF_DAY, -warningHours);
          if (now.after(warn)) {
            response.setAccountState(new EDirectoryAccountState(exp, loginRemainingValue));
          }
        } else {
          response.setAccountState(new EDirectoryAccountState(exp, loginRemainingValue));
        }
      } else if (loginRemaining != null) {
        response.setAccountState(new EDirectoryAccountState(null, loginRemainingValue));
      }
    }
  }
}
