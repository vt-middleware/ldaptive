/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Clock;
import java.time.Period;
import java.time.ZonedDateTime;
import org.ldaptive.AbstractFreezable;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ad.transcode.FileTimeValueTranscoder;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to parse the authentication response message and set the account state using data associated with active
 * directory. If this handler is assigned a {@link #expirationPeriod}, then the {@link org.ldaptive.auth.Authenticator}
 * should be configured to return the 'pwdLastSet' attribute, so it can be consumed by this handler. This will cause the
 * handler to emit a warning for the pwdLastSet value plus the expiration amount. The scope of that warning can be
 * further narrowed by providing a {@link #warningPeriod}. By default, if the msDS-UserPasswordExpiryTimeComputed
 * attribute is found, expirationPeriod is ignored.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandler extends AbstractFreezable
  implements AuthenticationResponseHandler
{

  /** Attributes needed to enforce password policy. */
  public static final String[] ATTRIBUTES = {"msDS-UserPasswordExpiryTimeComputed", "pwdLastSet", };

  /** Logger for this class. */
  private final Logger logger = LoggerFactory.getLogger(getClass());

  /** Clock to calculate current date for comparison with expiration time. */
  private final Clock expirationClock;

  /** Amount of time since a password was set until it will expire. Used if msDS-UserPasswordExpiryTimeComputed cannot
   * be read. */
  private Period expirationPeriod;

  /** Amount of time before expiration to produce a warning. */
  private Period warningPeriod;


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  clock  used to convert time before expiration to a datetime
   */
  ActiveDirectoryAuthenticationResponseHandler(final Clock clock)
  {
    expirationClock = clock;
  }


  /**
   * Creates a new active directory authentication response handler.
   */
  public ActiveDirectoryAuthenticationResponseHandler()
  {
    expirationClock = Clock.systemDefaultZone();
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  warning  length of time before expiration that should produce a warning
   */
  public ActiveDirectoryAuthenticationResponseHandler(final Period warning)
  {
    expirationClock = Clock.systemDefaultZone();
    setWarningPeriod(warning);
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  expiration  length of time that a password is valid
   * @param  warning  length of time before expiration that should produce a warning
   */
  public ActiveDirectoryAuthenticationResponseHandler(final Period expiration, final Period warning)
  {
    expirationClock = Clock.systemDefaultZone();
    setExpirationPeriod(expiration);
    setWarningPeriod(warning);
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.isSuccess()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry != null ? entry.getAttribute("msDS-UserPasswordExpiryTimeComputed") : null;
      final LdapAttribute pwdLastSet = entry != null ? entry.getAttribute("pwdLastSet") : null;

      logger.debug("Read attributes msDS-UserPasswordExpiryTimeComputed: {}, pwdLastSet: {}", expTime, pwdLastSet);
      ZonedDateTime exp = null;
      // ignore expTime if account is set to never expire
      if (expTime != null && !"9223372036854775807".equals(expTime.getStringValue())) {
        exp = expTime.getValue(new FileTimeValueTranscoder().decoder());
      } else if (expirationPeriod != null && pwdLastSet != null) {
        exp = pwdLastSet.getValue(new FileTimeValueTranscoder().decoder()).plus(expirationPeriod);
      }

      if (exp != null) {
        logger.debug("Transcoded passwordExpirationTime to {}", exp);
        if (warningPeriod != null) {
          final ZonedDateTime warn = exp.minus(warningPeriod);
          final ZonedDateTime now = ZonedDateTime.now(expirationClock);
          logger.debug("Warning period is: {}, current datetime is {}", warn, now);
          if (now.isAfter(warn)) {
            response.setAccountState(new ActiveDirectoryAccountState(exp));
          }
        } else {
          logger.debug("No warning period is defined");
          response.setAccountState(new ActiveDirectoryAccountState(exp));
        }
      }
    } else {
      if (response.getDiagnosticMessage() != null) {
        logger.debug("Parsing response diagnostic message: {}", response.getDiagnosticMessage());
        final ActiveDirectoryAccountState.Error adError = ActiveDirectoryAccountState.Error.parse(
          response.getDiagnosticMessage());
        if (adError != null) {
          logger.debug("Translated response diagnostic message to: {}", adError);
          response.setAccountState(new ActiveDirectoryAccountState(adError));
        }
      }
    }
    logger.debug("Configured authentication response: {}", response);
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
      "warningPeriod=" + warningPeriod + "]";
  }
}
