/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.ldaptive.auth.AccountState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the state of an Active Directory account. Note that the warning returned by this implementation always
 * returns -1 for logins remaining.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAccountState extends AccountState
{


  /**
   * Enum to define active directory errors. See http://ldapwiki.willeke.com/wiki/
   * Common%20Active%20Directory%20Bind%20Errors
   */
  public enum Error implements AccountState.Error {

    /** no such user. 0x525. */
    NO_SUCH_USER(1317),

    /** logon failure. 0x52e. */
    LOGON_FAILURE(1326),

    /** invalid logon hours. 0x530. */
    INVALID_LOGON_HOURS(1328),

    /** invalid workstation. 0x531. */
    INVALID_WORKSTATION(1329),

    /** password expired. 0x532. */
    PASSWORD_EXPIRED(1330),

    /** account disabled. 0x533. */
    ACCOUNT_DISABLED(1331),

    /** account expired. 0x701. */
    ACCOUNT_EXPIRED(1793),

    /** password must change. 0x773. */
    PASSWORD_MUST_CHANGE(1907),

    /** account locked out. 0x775. */
    ACCOUNT_LOCKED_OUT(1909);

    /** hex radix for hex to decimal conversion. */
    private static final int HEX_RADIX = 16;

    /** pattern to find hex code in active directory messages. */
    private static final Pattern PATTERN = Pattern.compile("data (\\w+)");

    /** underlying error code. */
    private final int code;


    /**
     * Creates a new active directory error.
     *
     * @param  i  error code
     */
    Error(final int i)
    {
      code = i;
    }


    @Override
    public int getCode()
    {
      return code;
    }


    @Override
    public String getMessage()
    {
      return name();
    }


    @Override
    public void throwSecurityException()
      throws LoginException
    {
      switch (this) {

      case NO_SUCH_USER:
        throw new AccountNotFoundException(name());

      case LOGON_FAILURE:
        throw new FailedLoginException(name());

      case INVALID_LOGON_HOURS:
        throw new AccountLockedException(name());

      case INVALID_WORKSTATION:
        throw new AccountException(name());

      case PASSWORD_EXPIRED:
        throw new CredentialExpiredException(name());

      case ACCOUNT_DISABLED:
        throw new AccountLockedException(name());

      case ACCOUNT_EXPIRED:
        throw new AccountExpiredException(name());

      case PASSWORD_MUST_CHANGE:
        throw new CredentialExpiredException(name());

      case ACCOUNT_LOCKED_OUT:
        throw new AccountLockedException(name());

      default:
        throw new IllegalStateException("Unknown active directory error: " + this);
      }
    }


    /**
     * Returns the error for the supplied integer constant.
     *
     * @param  code  to find error for
     *
     * @return  error
     */
    public static Error valueOf(final int code)
    {
      for (Error e : Error.values()) {
        if (e.getCode() == code) {
          return e;
        }
      }
      return null;
    }


    /**
     * Parses the supplied error messages and returns the corresponding error enum. Attempts to find {@link #PATTERN}
     * and parses the first group match as a hexadecimal integer.
     *
     * @param  message  to parse
     *
     * @return  active directory error
     */
    public static Error parse(final String message)
    {
      if (message != null) {
        final Matcher matcher = PATTERN.matcher(message);
        if (matcher.find()) {
          try {
            return Error.valueOf(Integer.parseInt(matcher.group(1).toUpperCase(), HEX_RADIX));
          } catch (NumberFormatException e) {
            final Logger l = LoggerFactory.getLogger(Error.class);
            l.warn("Error parsing active directory error", e);
          }
        }
      }
      return null;
    }
  }

  /** active directory specific enum. */
  private final Error adError;


  /**
   * Creates a new active directory account state.
   *
   * @param  exp  account expiration
   */
  public ActiveDirectoryAccountState(final ZonedDateTime exp)
  {
    super(new AccountState.DefaultWarning(exp, -1));
    adError = null;
  }


  /**
   * Creates a new active directory account state.
   *
   * @param  error  containing authentication failure details
   */
  public ActiveDirectoryAccountState(final ActiveDirectoryAccountState.Error error)
  {
    super(error);
    adError = error;
  }


  /**
   * Returns the active directory error for this account state.
   *
   * @return  active directory error
   */
  public ActiveDirectoryAccountState.Error getActiveDirectoryError()
  {
    return adError;
  }
}
