/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZonedDateTime;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;

/**
 * Attempts to parse the authentication response and set the account state using data associated with eDirectory. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'passwordExpirationTime' and
 * 'loginGraceRemaining' attributes so they can be consumed by this handler. If this handler is assigned a {@link
 * #warningPeriod}, this handler will only emit warnings during that window before password expiration. Otherwise,
 * a warning is always emitted if passwordExpirationTime is set.
 *
 * @author  Middleware Services
 */
public class EDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Amount of time before expiration to produce a warning. */
  private Period warningPeriod;


  /** Default constructor. */
  public EDirectoryAuthenticationResponseHandler() {}


  /**
   * Creates a new edirectory authentication response handler.
   *
   * @param  warning  length of time before expiration that should produce a warning
   */
  public EDirectoryAuthenticationResponseHandler(final Period warning)
  {
    if (warning == null) {
      throw new IllegalArgumentException("Warning cannot be null");
    }
    warningPeriod = warning;
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
        final ZonedDateTime exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
        if (warningPeriod != null) {
          final ZonedDateTime warn = exp.minus(warningPeriod);
          if (ZonedDateTime.now().isAfter(warn)) {
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

  /**
   * Get amount of time before expiration to produce a warning.
   * @return warning period
   */
  public Period getWarningPeriod()
  {
    return warningPeriod;
  }

  /**
   * Set amount of time before expiration to produce a warning.
   * @param period warning period
   */
  public void setWarningPeriod(final Period period)
  {
    this.warningPeriod = period;
  }
}
