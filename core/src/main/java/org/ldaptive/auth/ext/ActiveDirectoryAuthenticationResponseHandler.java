/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZonedDateTime;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ad.io.FileTimeValueTranscoder;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;

/**
 * Attempts to parse the authentication response message and set the account state using data associated with active
 * directory. If this handler is assigned a {@link #expirationPeriod}, then the {@link org.ldaptive.auth.Authenticator}
 * should be configured to return the 'pwdLastSet' attribute so it can be consumed by this handler. This will cause the
 * handler to emit a warning for the pwdLastSet value plus the expiration amount. The scope of that warning can be
 * further narrowed by providing a {@link #warningPeriod}.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  /** Amount of time since a password was set until it will expire. */
  private Period expirationPeriod;

  /** Amount of time before expiration to produce a warning. */
  private Period warningPeriod;


  /** Default constructor. */
  public ActiveDirectoryAuthenticationResponseHandler() {}


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  expiration  length of time that a password is valid
   */
  public ActiveDirectoryAuthenticationResponseHandler(final Period expiration)
  {
    setExpirationPeriod(expiration);
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  expiration  length of time that a password is valid
   * @param  warning  length of time before expiration that should produce a warning
   */
  public ActiveDirectoryAuthenticationResponseHandler(final Period expiration, final Period warning)
  {
    setExpirationPeriod(expiration);
    setWarningPeriod(warning);
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResult()) {
      if (expirationPeriod != null) {
        final LdapEntry entry = response.getLdapEntry();
        final LdapAttribute pwdLastSet = entry.getAttribute("pwdLastSet");
        if (pwdLastSet != null) {
          final ZonedDateTime exp = pwdLastSet.getValue(new FileTimeValueTranscoder()).plus(expirationPeriod);
          if (warningPeriod != null) {
            final ZonedDateTime warn = exp.minus(warningPeriod);
            if (ZonedDateTime.now().isAfter(warn)) {
              response.setAccountState(new ActiveDirectoryAccountState(exp));
            }
          } else {
            response.setAccountState(new ActiveDirectoryAccountState(exp));
          }
        }
      }
    } else {
      if (response.getMessage() != null) {
        final ActiveDirectoryAccountState.Error adError = ActiveDirectoryAccountState.Error.parse(
          response.getMessage());
        if (adError != null) {
          response.setAccountState(new ActiveDirectoryAccountState(adError));
        }
      }
    }
  }


  /**
   * Returns the amount of time since a password was set until it will expire.
   *
   * @return  expiration period
   */
  public Period getExpirationPeriod()
  {
    return expirationPeriod;
  }


  /**
   * Sets amount of time since a password was set until it will expire.
   *
   * @param  period  expiration period
   */
  public void setExpirationPeriod(final Period period)
  {
    if (period == null) {
      throw new IllegalArgumentException("Expiration cannot be null");
    }
    expirationPeriod = period;
  }


  /**
   * Returns the amount of time before expiration to produce a warning.
   *
   * @return  warning period
   */
  public Period getWarningPeriod()
  {
    return warningPeriod;
  }


  /**
   * Sets the amount of time before expiration to produce a warning.
   *
   * @param  period  warning period
   */
  public void setWarningPeriod(final Period period)
  {
    if (period == null) {
      throw new IllegalArgumentException("Warning cannot be null");
    }
    warningPeriod = period;
  }


  @Override
  public String toString()
  {
    return String.format(
      "[%s@%d::expirationPeriod=%s, warningPeriod=%s]",
      getClass().getName(),
      hashCode(),
      expirationPeriod,
      warningPeriod);
  }
}
