/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZonedDateTime;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;


/**
 * Attempts to parse the authentication response and set the account state using data associated with FreeIPA. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'krbPasswordExpiration',
 * 'krbLoginFailedCount' and 'krbLastPwdChange' attributes, so they can be consumed by this handler.
 *
 * @author  tduehr
 */
public final class FreeIPAAuthenticationResponseHandler extends AbstractFreezable
  implements AuthenticationResponseHandler
{

  /** Attributes needed to enforce password policy. */
  public static final String[] ATTRIBUTES = new String[] {
    "krbPasswordExpiration",
    "krbLoginFailedCount",
    "krbLastPwdChange",
  };

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
    setWarningPeriod(warning);
    setMaxLoginFailures(loginFailures);
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
    setExpirationPeriod(expiration);
    setWarningPeriod(warning);
    setMaxLoginFailures(loginFailures);
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResultCode() != ResultCode.SUCCESS) {
      final FreeIPAAccountState.Error fError = FreeIPAAccountState.Error.parse(
        response.getResultCode(),
        response.getDiagnosticMessage());
      if (fError != null) {
        response.setAccountState(new FreeIPAAccountState(fError));
      }
    } else if (response.isSuccess()) {
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
        exp = expTime.getValue(new GeneralizedTimeValueTranscoder().decoder());
      } else if (expirationPeriod != null && lastPwdChange != null) {
        exp = lastPwdChange.getValue(new GeneralizedTimeValueTranscoder().decoder()).plus(expirationPeriod);
      }
      if (exp != null) {
        if (warningPeriod != null) {
          final ZonedDateTime warn = exp.minus(warningPeriod);
          if (ZonedDateTime.now().isAfter(warn)) {
            response.setAccountState(
              new FreeIPAAccountState(exp, loginRemaining != null ? loginRemaining : 0));
          }
        } else {
          response.setAccountState(
            new FreeIPAAccountState(exp, loginRemaining != null ? loginRemaining : 0));
        }
      } else if (loginRemaining != null && loginRemaining < maxLoginFailures) {
        response.setAccountState(new FreeIPAAccountState(null, loginRemaining));
      }
    }
  }


  /**
   * Returns the maximum login failures.
   *
   * @return  maximum login failures before lockout.
   */
  public int getMaxLoginFailures()
  {
    return maxLoginFailures;
  }


  /**
   * Sets the maximum login failures.
   *
   * @param  loginFailures  before lockout.
   */
  public void setMaxLoginFailures(final int loginFailures)
  {
    assertMutable();
    if (loginFailures < 0) {
      throw new IllegalArgumentException("Login failures must be >= 0");
    }
    maxLoginFailures = loginFailures;
  }


  /**
   * Returns the amount of time since a password was set until it will expire. Only used if the krbPasswordExpiration
   * attribute cannot be read from the directory.
   *
   * @return  expiration period
   */
  public Period getExpirationPeriod()
  {
    return expirationPeriod;
  }


  /**
   * Sets the amount of time since a password was set until it will expire. Only used if the krbPasswordExpiration
   * attribute cannot be read from the directory.
   *
   * @param  period  expiration period
   */
  public void setExpirationPeriod(final Period period)
  {
    assertMutable();
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
    assertMutable();
    warningPeriod = period;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "expirationPeriod=" + expirationPeriod + ", " +
      "warningPeriod=" + warningPeriod + ", " +
      "maxLoginFailures=" + maxLoginFailures + "]";
  }
}
