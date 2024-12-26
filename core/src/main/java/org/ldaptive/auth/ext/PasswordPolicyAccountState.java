/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AccountState;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Represents the state of an account as described by a password policy control. The {@link PasswordPolicyControl}
 * supports a single warning and/or a single error.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAccountState extends AccountState
{

  /** password policy specific enum. */
  private final PasswordPolicyControl.Error ppError;


  /**
   * Creates a new password policy account state with a timeBeforeExpiration warning.
   *
   * @param  exp  account expiration
   * @param  remaining  number of logins available
   */
  public PasswordPolicyAccountState(final ZonedDateTime exp, final int remaining)
  {
    super(new AccountState.DefaultWarning(exp, remaining));
    ppError = null;
  }


  /**
   * Creates a new password policy account state.
   *
   * @param  exp  account expiration
   */
  public PasswordPolicyAccountState(final ZonedDateTime exp)
  {
    super(new AccountState.DefaultWarning(exp, -1));
    ppError = null;
  }


  /**
   * Creates a new password policy account state with a graceAuthNsRemaining warning.
   *
   * @param  remaining  number of logins available
   */
  public PasswordPolicyAccountState(final int remaining)
  {
    super(new AccountState.DefaultWarning(null, remaining));
    ppError = null;
  }


  /**
   * Creates a new password policy account state with an error.
   *
   * @param  error  containing password policy error details
   */
  public PasswordPolicyAccountState(final PasswordPolicyControl.Error error)
  {
    super(error);
    ppError = error;
  }


  /**
   * Creates a new password policy account state with both a timeBeforeExpiration warning and an error.
   *
   * @param  exp  account expiration
   * @param  error  containing password policy error details
   */
  public PasswordPolicyAccountState(final ZonedDateTime exp, final PasswordPolicyControl.Error error)
  {
    super(new AccountState.DefaultWarning(exp, -1), error);
    ppError = error;
  }


  /**
   * Creates a new password policy account state with both a graceAuthNsRemaining warning and an error.
   *
   * @param  remaining  number of logins available
   * @param  error  containing password policy error details
   */
  public PasswordPolicyAccountState(final int remaining, final PasswordPolicyControl.Error error)
  {
    super(new AccountState.DefaultWarning(null, remaining), error);
    ppError = error;
  }


  /**
   * Returns the password policy error for this account state.
   *
   * @return  password policy error
   */
  public PasswordPolicyControl.Error getPasswordPolicyError()
  {
    return ppError;
  }
}
