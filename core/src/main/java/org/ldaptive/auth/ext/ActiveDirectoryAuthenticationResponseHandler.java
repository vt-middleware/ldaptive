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
 * further narrow by providing a {@link #warningPeriod}.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  /** Amount of time since a password was set that it will expire. */
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
    if (expiration == null) {
      throw new IllegalArgumentException("Expiration cannot be null");
    }
    expirationPeriod = expiration;
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  expiration  length of time that a password is valid
   * @param  warning  length of time before expiration that should produce a warning
   */
  public ActiveDirectoryAuthenticationResponseHandler(final Period expiration, final Period warning)
  {
    if (expiration == null) {
      throw new IllegalArgumentException("Expiration cannot be null");
    }
    expirationPeriod = expiration;
    if (warning == null) {
      throw new IllegalArgumentException("Warning cannot be null");
    }
    warningPeriod = warning;
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
}
