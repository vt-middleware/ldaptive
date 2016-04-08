/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attempts to parse the authentication response and set the account state using data associated with FreeIPA. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'krbPasswordExpiration',
 * 'krbLoginFailedCount' and 'krbLastPwdChange' attributes so they can be consumed by this handler.
 *
 * @author  tduehr
 */
public class FreeIPAAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Maximum password age. */
  private int maxPasswordAge;

  /** Maximum password age. */
  private int maxLoginFailures;

  /** Number of hours before expiration to produce a warning. */
  private int warningHours;


  /** Default constructor. */
  public FreeIPAAuthenticationResponseHandler() {}


  /**
   * Creates a new freeipa authentication response handler.
   *
   * @param  warning  length of time before expiration that should produce a warning
   * @param  passwordAge  length of time in days that a password is valid
   * @param  loginFailures  number of login failures to allow
   */
  public FreeIPAAuthenticationResponseHandler(final int warning, final int passwordAge, final int loginFailures)
  {
    if (warning < 0) {
      throw new IllegalArgumentException("Warning hours must be >= 0");
    }
    warningHours = warning;
    if (passwordAge < 0) {
      throw new IllegalArgumentException("Password age must be >= 0");
    }
    maxPasswordAge = passwordAge;
    if (loginFailures < 0) {
      throw new IllegalArgumentException("Login failures must be >= 0");
    }
    maxLoginFailures = loginFailures;
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResultCode() != ResultCode.SUCCESS) {
      final FreeIPAAccountState.Error fError = FreeIPAAccountState.Error.parse(
        response.getResultCode(),
        response.getMessage());
      if (fError != null) {
        response.setAccountState(new FreeIPAAccountState(fError));
      }
    } else if (response.getResult()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("krbPasswordExpiration");
      final LdapAttribute failedLogins = entry.getAttribute("krbLoginFailedCount");
      final LdapAttribute lastPwdChange = entry.getAttribute("krbLastPwdChange");
      Calendar exp = null;

      Integer loginRemaining = null;
      if (failedLogins != null && maxLoginFailures > 0) {
        loginRemaining = maxLoginFailures - Integer.parseInt(failedLogins.getStringValue());
      }

      final Calendar now = Calendar.getInstance();
      if (expTime != null) {
        exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
      } else if (maxPasswordAge > 0 && lastPwdChange != null) {
        exp = lastPwdChange.getValue(new GeneralizedTimeValueTranscoder());
        exp.setTimeInMillis(exp.getTimeInMillis() + TimeUnit.DAYS.toMillis(maxPasswordAge));
      }
      if (exp != null) {
        if (warningHours > 0) {
          final Calendar warn = (Calendar) exp.clone();
          warn.add(Calendar.HOUR_OF_DAY, -warningHours);
          if (now.after(warn)) {
            response.setAccountState(
              new FreeIPAAccountState(exp, loginRemaining != null ? loginRemaining.intValue() : 0));
          }
        } else {
          response.setAccountState(
            new FreeIPAAccountState(exp, loginRemaining != null ? loginRemaining.intValue() : 0));
        }
      } else if (loginRemaining != null) {
        response.setAccountState(new FreeIPAAccountState(null, loginRemaining));
      }
    }
  }


  @Override
  public String toString()
  {
    return String.format(
      "[%s@%d::maxPasswordAge=%s, maxLoginFailures=%s, warningHours=%s]",
      getClass().getName(),
      hashCode(),
      maxPasswordAge,
      maxLoginFailures,
      warningHours);
  }
}
