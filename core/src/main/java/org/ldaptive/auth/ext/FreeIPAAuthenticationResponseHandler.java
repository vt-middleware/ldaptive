/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZonedDateTime;
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

  /** Amount of time since a password was set until it will expire. Used if krbPasswordExpiration cannot be read. */
  private Period expirationPeriod;

  /** Amount of time before expiration to produce a warning. */
  private Period warningPeriod;

  /** Maximum number of login failures to allow. */
  private int maxLoginFailures;


  /** Default constructor. */
  public FreeIPAAuthenticationResponseHandler() {}


  /**
   * Creates a new freeipa authentication response handler.
   *
   * @param  warning  length of time before expiration that should produce a warning
   * @param  loginFailures  number of login failures to allow
   */
  public FreeIPAAuthenticationResponseHandler(final Period warning, final int loginFailures)
  {
    if (warning == null) {
      throw new IllegalArgumentException("Warning cannot be null");
    }
    warningPeriod = warning;
    if (loginFailures < 0) {
      throw new IllegalArgumentException("Login failures must be >= 0");
    }
    maxLoginFailures = loginFailures;
  }


  /**
   * Creates a new freeipa authentication response handler.
   *
   * @param  expiration  length of time that a password is valid
   * @param  warning  length of time before expiration that should produce a warning
   * @param  loginFailures  number of login failures to allow
   */
  public FreeIPAAuthenticationResponseHandler(final Period expiration, final Period warning, final int loginFailures)
  {
    if (expiration == null) {
      throw new IllegalArgumentException("Expiration cannot be null");
    }
    expirationPeriod = expiration;
    if (warning == null) {
      throw new IllegalArgumentException("Warning cannot be null");
    }
    warningPeriod = warning;
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
      ZonedDateTime exp = null;

      Integer loginRemaining = null;
      if (failedLogins != null && maxLoginFailures > 0) {
        loginRemaining = maxLoginFailures - Integer.parseInt(failedLogins.getStringValue());
      }

      if (expTime != null) {
        exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
      } else if (expirationPeriod != null && lastPwdChange != null) {
        exp = lastPwdChange.getValue(new GeneralizedTimeValueTranscoder()).plus(expirationPeriod);
      }
      if (exp != null) {
        if (warningPeriod != null) {
          final ZonedDateTime warn = exp.minus(warningPeriod);
          if (ZonedDateTime.now().isAfter(warn)) {
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
}
