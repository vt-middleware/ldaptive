/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.ldaptive.ResultCode;
import static org.ldaptive.ResultCode.*;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the state of an FreeIPA account.
 *
 * @author  tduehr
 */
public class FreeIPAAccountState extends AccountState
{

  public enum Error implements AccountState.Error {

    UNKNOWN(-1),
    FAILED_AUTHENTICATION(1),
    PASSWORD_EXPIRED(2),
    ACCOUNT_EXPIRED(3),
    MAXIMUM_LOGINS_EXCEEDED(4),
    LOGIN_TIME_LIMITED(5),
    LOGIN_LOCKOUT(6),
    ACCOUNT_NOT_FOUND(7),
    CREDENTIAL_NOT_FOUND(8),
    ACCOUNT_DISABLED(9);

    /** underlying error code. */
    private final int code;

    /**
     * Creates a new freeipa error.
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

      case ACCOUNT_NOT_FOUND:
        throw new AccountNotFoundException(name());

      case FAILED_AUTHENTICATION:
        throw new FailedLoginException(name());

      case ACCOUNT_DISABLED:
        throw new FailedLoginException(name());

      case PASSWORD_EXPIRED:
        throw new CredentialExpiredException(name());

      case CREDENTIAL_NOT_FOUND:
        throw new FailedLoginException(name());

      case ACCOUNT_EXPIRED:
        throw new AccountExpiredException(name());

      case MAXIMUM_LOGINS_EXCEEDED:
        throw new AccountLockedException(name());

      case LOGIN_TIME_LIMITED:
        throw new AccountLockedException(name());

      case LOGIN_LOCKOUT:
        throw new AccountLockedException(name());

      case UNKNOWN:
        throw new FailedLoginException(name());

      default:
        throw new IllegalStateException("Unknown FreeIPA error: " + this);
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
      if (ResultCode.valueOf(code) == ResultCode.SUCCESS)
        return null;
      else
        return UNKNOWN;
    }


    /**
     * Parses the supplied error messages and returns the corresponding error enum. Attempts to find {@link #PATTERN}
     * and parses the second group match as a decimal integer.
     *
     * @param  message  to parse
     *
     * @return  freeipa error
     */
    public static Error parse(final ResultCode rc, String message)
    {
      if (rc != null && rc != SUCCESS) {
        switch(rc) {

        case NO_SUCH_OBJECT:
          return ACCOUNT_NOT_FOUND;

        case INVALID_CREDENTIALS:
          return CREDENTIAL_NOT_FOUND;

        case INSUFFICIENT_ACCESS_RIGHTS:
          return FAILED_AUTHENTICATION;
        case UNWILLING_TO_PERFORM:
          if (message.equals("Entry permanently locked.\n"))
            return LOGIN_LOCKOUT;
          else if (message.equals("Too many failed logins.\n"))
            return MAXIMUM_LOGINS_EXCEEDED;
          else if (message.equals("Account (Kerberos principal) is expired"))
            return ACCOUNT_EXPIRED;
          else if (message.equals("Account inactivated. Contact system administrator."))
            return ACCOUNT_DISABLED;
        
        default:
          return UNKNOWN;
        }
      }
      return null;
    }
  }

  /** freeipa specific enum. */
  private final Error fError;


  /**
   * Creates a new edirectory account state.
   *
   * @param  exp  account expiration
   * @param  remaining  number of logins available
   */
  public FreeIPAAccountState(final Calendar exp, final int remaining)
  {
    super(new AccountState.DefaultWarning(exp, remaining));
    fError = null;
  }


  /**
   * Creates a new edirectory account state.
   *
   * @param  error  containing authentication failure details
   */
  public FreeIPAAccountState(final FreeIPAAccountState.Error error)
  {
    super(error);
    fError = error;
  }


  /**
   * Returns the edirectory error for this account state.
   *
   * @return  freeipa error
   */
  public FreeIPAAccountState.Error getFreeIPAError()
  {
    return fError;
  }
}
