/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.time.ZonedDateTime;
import java.util.Arrays;
import javax.security.auth.login.LoginException;

/**
 * Represents the state of an LDAP account based on account policies for that LDAP. Note that only warning(s) or
 * error(s) may be set, not both.
 *
 * @author  Middleware Services
 */
public class AccountState
{

  /** account warning. */
  private final AccountState.Warning[] accountWarnings;

  /** account error. */
  private final AccountState.Error[] accountErrors;


  /**
   * Creates a new account state.
   *
   * @param  warnings  associated with the account
   */
  public AccountState(final AccountState.Warning... warnings)
  {
    accountWarnings = warnings;
    accountErrors = null;
  }


  /**
   * Creates a new account state.
   *
   * @param  errors  associated with the account
   */
  public AccountState(final AccountState.Error... errors)
  {
    accountWarnings = null;
    accountErrors = errors;
  }


  /**
   * Returns the account state warnings.
   *
   * @return  account state warnings
   */
  public AccountState.Warning[] getWarnings()
  {
    return accountWarnings;
  }


  /**
   * Returns the first account state warning or null if no warnings exist.
   *
   * @return  first account state warning
   */
  public AccountState.Warning getWarning()
  {
    return accountWarnings != null && accountWarnings.length > 0 ? accountWarnings[0] : null;
  }


  /**
   * Returns the account state errors.
   *
   * @return  account state errors
   */
  public AccountState.Error[] getErrors()
  {
    return accountErrors;
  }


  /**
   * Returns the first account state error or null if no errors exist.
   *
   * @return  first account state error
   */
  public AccountState.Error getError()
  {
    return accountErrors != null && accountErrors.length > 0 ? accountErrors[0] : null;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::accountWarnings=%s, accountErrors=%s]",
        getClass().getName(),
        hashCode(),
        Arrays.toString(accountWarnings),
        Arrays.toString(accountErrors));
  }


  /** Contains error information for an account state. */
  public interface Error
  {


    /**
     * Returns the error code.
     *
     * @return  error code
     */
    int getCode();


    /**
     * Returns the error message.
     *
     * @return  error message
     */
    String getMessage();


    /**
     * Throws the LoginException that best maps to this error.
     *
     * @throws  LoginException  for this account state error
     */
    void throwSecurityException()
      throws LoginException;
  }


  /** Contains warning information for an account state. */
  public interface Warning
  {


    /**
     * Returns the expiration.
     *
     * @return  expiration
     */
    ZonedDateTime getExpiration();


    /**
     * Returns the number of logins remaining until the account locks.
     *
     * @return  number of logins remaining
     */
    int getLoginsRemaining();
  }


  /** Default warning implementation. */
  public static class DefaultWarning implements Warning
  {

    /** expiration. */
    private final ZonedDateTime expiration;

    /** number of logins remaining before the account locks. */
    private final int loginsRemaining;


    /**
     * Creates a new warning.
     *
     * @param  exp  date of expiration
     * @param  remaining  number of logins
     */
    public DefaultWarning(final ZonedDateTime exp, final int remaining)
    {
      expiration = exp;
      loginsRemaining = remaining;
    }


    @Override
    public ZonedDateTime getExpiration()
    {
      return expiration;
    }


    @Override
    public int getLoginsRemaining()
    {
      return loginsRemaining;
    }


    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::expiration=%s, loginsRemaining=%s]",
          getClass().getName(),
          hashCode(),
          expiration,
          loginsRemaining);
    }
  }
}
