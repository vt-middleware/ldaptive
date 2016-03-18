/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ad.io.FileTimeValueTranscoder;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;

/**
 * Attempts to parse the authentication response message and set the account state using data associated with active
 * directory.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  /** Amount of time in milliseconds since a password was set until it will expire. Used if
   * msDS-UserPasswordExpiryTimeComputed cannot be read. */
  private long maxPasswordAge = -1;

  /** Number of hours before expiration to produce a warning. */
  private int warningHours;


  /** Default constructor. */
  public ActiveDirectoryAuthenticationResponseHandler() {}


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  passwordAge  length of time in milliseconds that a password is valid
   */
  public ActiveDirectoryAuthenticationResponseHandler(final long passwordAge)
  {
    if (passwordAge < 0) {
      throw new IllegalArgumentException("Password age must be >= 0");
    }
    maxPasswordAge = passwordAge;
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  hours  length of time before expiration that should produce a warning
   */
  public ActiveDirectoryAuthenticationResponseHandler(final int hours)
  {
    if (hours <= 0) {
      throw new IllegalArgumentException("Hours must be > 0");
    }
    warningHours = hours;
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  hours  length of time before expiration that should produce a warning
   * @param  passwordAge  length of time in milliseconds that a password is valid
   */
  public ActiveDirectoryAuthenticationResponseHandler(final int hours, final long passwordAge)
  {
    if (hours <= 0) {
      throw new IllegalArgumentException("Hours must be > 0");
    }
    warningHours = hours;
    if (passwordAge < 0) {
      throw new IllegalArgumentException("Password age must be >= 0");
    }
    maxPasswordAge = passwordAge;
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResult()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("msDS-UserPasswordExpiryTimeComputed");
      final LdapAttribute pwdLastSet = entry.getAttribute("pwdLastSet");

      Calendar exp = null;
      // ignore expTime if account is set to never expire
      if (expTime != null && !"9223372036854775807".equals(expTime.getStringValue())) {
        exp = expTime.getValue(new FileTimeValueTranscoder());
      } else if (maxPasswordAge >= 0 && pwdLastSet != null) {
        exp = pwdLastSet.getValue(new FileTimeValueTranscoder());
        exp.setTimeInMillis(exp.getTimeInMillis() + maxPasswordAge);
      }

      if (exp != null) {
        if (warningHours > 0) {
          final Calendar now = Calendar.getInstance();
          final Calendar warn = (Calendar) exp.clone();
          warn.add(Calendar.HOUR_OF_DAY, -warningHours);
          if (now.after(warn)) {
            response.setAccountState(new ActiveDirectoryAccountState(exp));
          }
        } else {
          response.setAccountState(new ActiveDirectoryAccountState(exp));
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


  @Override
  public String toString()
  {
    return String.format(
      "[%s@%d::maxPasswordAge=%s, warningHours=%s]",
      getClass().getName(),
      hashCode(),
      maxPasswordAge,
      warningHours);
  }
}
