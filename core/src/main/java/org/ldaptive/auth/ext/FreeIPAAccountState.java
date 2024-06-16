/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AccountState;

/**
 * Represents the state of a FreeIPA account.
 *
 * @author  tduehr
 */
public class FreeIPAAccountState extends AccountState
{


  /**
   * Enum to define FreeIPA errors.
   */
  public enum Error implements AccountState.Error {

    /** unknown state. */
    UNKNOWN(-1),

    /** failed authentication. */
    FAILED_AUTHENTICATION(1),

    /** password expired. */
    PASSWORD_EXPIRED(2),

    /** account expired. */
    ACCOUNT_EXPIRED(3),

    /** maximum logins exceeded. */
    MAXIMUM_LOGINS_EXCEEDED(4),

    /** login time limited. */
    LOGIN_TIME_LIMITED(5),

    /** login lockout. */
    LOGIN_LOCKOUT(6),

    /** account not found. */
    ACCOUNT_NOT_FOUND(7),

    /** credential not found. */
    CREDENTIAL_NOT_FOUND(8),

    /** account disabled. */
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

      case ACCOUNT_DISABLED:

      case CREDENTIAL_NOT_FOUND:

      case UNKNOWN:
        throw new FailedLoginException(name());

      case PASSWORD_EXPIRED:
        throw new CredentialExpiredException(name());

      case ACCOUNT_EXPIRED:
        throw new AccountExpiredException(name());

      case MAXIMUM_LOGINS_EXCEEDED:

      case LOGIN_TIME_LIMITED:

      case LOGIN_LOCKOUT:
        throw new AccountLockedException(name());

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
      for (Error e : values()) {
        if (e.getCode() == code) {
          return e;
        }
      }
      return ResultCode.valueOf(code) == ResultCode.SUCCESS ? null : UNKNOWN;
    }


    /**
     * Parses the supplied error messages and returns the corresponding error enum.
     *
     * @param  rc  result code
     * @param  message  to parse
     *
     * @return  freeipa error
     */
    public static Error parse(final ResultCode rc, final String message)
    {
      Error error = null;
      if (rc != null && rc != ResultCode.SUCCESS) {
        if (rc == ResultCode.NO_SUCH_OBJECT) {
          error = ACCOUNT_NOT_FOUND;
        } else if (rc == ResultCode.INVALID_CREDENTIALS) {
          error = CREDENTIAL_NOT_FOUND;
        } else if (rc == ResultCode.INSUFFICIENT_ACCESS_RIGHTS) {
          error = FAILED_AUTHENTICATION;
        } else if (rc == ResultCode.UNWILLING_TO_PERFORM) {
          if ("Entry permanently locked.\n".equals(message)) {
            error =  LOGIN_LOCKOUT;
          } else if ("Too many failed logins.\n".equals(message)) {
            error =  MAXIMUM_LOGINS_EXCEEDED;
          } else if ("Account (Kerberos principal) is expired".equals(message)) {
            error =  ACCOUNT_EXPIRED;
          } else if ("Account inactivated. Contact system administrator.".equals(message)) {
            error =  ACCOUNT_DISABLED;
          }
        } else {
          error =  UNKNOWN;
        }
      }
      return error;
    }
  }

  /** freeipa specific enum. */
  private final Error fError;


  /**
   * Creates a new freeipa account state.
   *
   * @param  exp  account expiration
   * @param  remaining  number of logins available
   */
  public FreeIPAAccountState(final ZonedDateTime exp, final int remaining)
  {
    super(new AccountState.DefaultWarning(exp, remaining));
    fError = null;
  }


  /**
   * Creates a new freeipa account state.
   *
   * @param  error  containing authentication failure details
   */
  public FreeIPAAccountState(final FreeIPAAccountState.Error error)
  {
    super(error);
    fError = error;
  }


  /**
   * Returns the freeipa error for this account state.
   *
   * @return  freeipa error
   */
  public FreeIPAAccountState.Error getFreeIPAError()
  {
    return fError;
  }
}
