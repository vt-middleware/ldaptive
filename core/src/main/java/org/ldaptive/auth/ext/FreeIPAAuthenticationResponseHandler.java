/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import static org.ldaptive.ResultCode.*;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Attempts to parse the authentication response and set the account state using data associated with eDirectory. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'passwordExpirationTime' and
 * 'loginGraceRemaining' attributes so they can be consumed by this handler.
 *
 * @author  tduehr
 */
public class FreeIPAAuthenticationResponseHandler implements AuthenticationResponseHandler
{

  /** Maximum password age. */
  private int maxPasswordAge = -1;

  /** Maximum password age. */
  private int maxLoginFailures = -1;

  /** Number of hours before expiration to produce a warning. */
  private int warningHours;

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Default constructor. */
  public FreeIPAAuthenticationResponseHandler() {}

  /**
   * Creates a new edirectory authentication response handler.
   *
   * @param  hours  length of time before expiration that should produce a warning
   */
  public FreeIPAAuthenticationResponseHandler(final int hours)
  {
    if (hours <= 0) {
      throw new IllegalArgumentException("Hours must be > 0");
    }
    warningHours = hours;
  }

  public int getMaxPasswordAge(){
    return maxPasswordAge;
  }
  
  public void setMaxPasswordAge(final int age){
    if (age <= 0) {
      throw new IllegalArgumentException("Age must be > 0");
    }
    maxPasswordAge = age;
  }

  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResultCode() != SUCCESS) {
      final FreeIPAAccountState.Error fError = FreeIPAAccountState.Error.parse(response.getResultCode(), response.getMessage());
      if (fError != null) {
        response.setAccountState(new FreeIPAAccountState(fError));
      }
    } else if (response.getResult()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("krbPasswordExpiration");
      logger.info("krbPasswordExpiration: {}", expTime);
      final LdapAttribute loginRemaining = entry.getAttribute("loginGraceRemaining");
      logger.info("loginGraceRemaining: {}", loginRemaining);
      final LdapAttribute failedLogins = entry.getAttribute("krbLoginFailedCount");
      logger.info("krbLoginFailedCount: {}", failedLogins);
      final LdapAttribute lastPwdChange = entry.getAttribute("krbLastPwdChange");
      logger.info("krbLastPwdChange: {}", lastPwdChange);
      Calendar exp = null;

      int loginRemainingValue = 0;
      if (loginRemaining != null)
        loginRemainingValue = Integer.parseInt(loginRemaining.getStringValue());
      else if (failedLogins != null && maxLoginFailures >= 0)
        loginRemainingValue = maxLoginFailures - Integer.parseInt(failedLogins.getStringValue());

      final Calendar now = Calendar.getInstance();
      if (expTime != null) {
        exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
      } else if (maxPasswordAge >= 0 && lastPwdChange != null) {
        exp = lastPwdChange.getValue(new GeneralizedTimeValueTranscoder());
        exp.setTimeInMillis(exp.getTimeInMillis() + (maxPasswordAge * 24 * 3600000));
      }
      if (warningHours > 0) {
        final Calendar warn = (Calendar) exp.clone();
        warn.add(Calendar.HOUR_OF_DAY, -warningHours);
        if (now.before(warn))
          exp = null;
      }
      response.setAccountState(new FreeIPAAccountState(exp, loginRemainingValue));
    }
  }
}
