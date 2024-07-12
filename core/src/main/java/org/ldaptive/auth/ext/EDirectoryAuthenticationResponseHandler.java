/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZonedDateTime;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to parse the authentication response and set the account state using data associated with eDirectory. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'passwordExpirationTime' and
 * 'loginGraceRemaining' attributes, so they can be consumed by this handler. If this handler is assigned a {@link
 * #warningPeriod}, this handler will only emit warnings during that window before password expiration. Otherwise,
 * a warning is always emitted if passwordExpirationTime is set.
 *
 * @author  Middleware Services
 */
public class EDirectoryAuthenticationResponseHandler extends AbstractFreezable
  implements AuthenticationResponseHandler
{

  /** Attributes needed to enforce password policy. */
  public static final String[] ATTRIBUTES = {"passwordExpirationTime", "loginGraceRemaining", };

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

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
    setWarningPeriod(warning);
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getDiagnosticMessage() != null) {
      logger.debug("Parsing response diagnostic message: {}", response.getDiagnosticMessage());
      final EDirectoryAccountState.Error edError = EDirectoryAccountState.Error.parse(response.getDiagnosticMessage());
      if (edError != null) {
        logger.debug("Translated response diagnostic message to: {}", edError);
        response.setAccountState(new EDirectoryAccountState(edError));
      }
    } else if (response.isSuccess()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("passwordExpirationTime");
      final LdapAttribute loginRemaining = entry.getAttribute("loginGraceRemaining");
      final int loginRemainingValue = loginRemaining != null ? Integer.parseInt(loginRemaining.getStringValue()) : 0;

      logger.debug("Read attributes passwordExpirationTime: {}, loginGraceRemaining: {}", expTime, loginRemaining);
      if (expTime != null) {
        final ZonedDateTime exp = expTime.getValue(new GeneralizedTimeValueTranscoder().decoder());
        logger.debug("Transcoded passwordExpirationTime to {}", exp);
        if (warningPeriod != null) {
          final ZonedDateTime warn = exp.minus(warningPeriod);
          final ZonedDateTime now = ZonedDateTime.now();
          logger.debug("Warning period is: {}, current datetime is {}", warn, now);
          if (now.isAfter(warn)) {
            response.setAccountState(new EDirectoryAccountState(exp, loginRemainingValue));
          }
        } else {
          logger.debug("No warning period is defined");
          response.setAccountState(new EDirectoryAccountState(exp, loginRemainingValue));
        }
      } else if (loginRemaining != null) {
        logger.debug("Using loginGraceRemaining: {}", loginRemainingValue);
        response.setAccountState(new EDirectoryAccountState(null, loginRemainingValue));
      }
    }
    logger.debug("Configured authentication response: {}", response);
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
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "warningPeriod=" + warningPeriod + "]";
  }
}
