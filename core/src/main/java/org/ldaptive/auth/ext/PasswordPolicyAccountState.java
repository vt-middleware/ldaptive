/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AccountState;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Represents the state of an account as described by a password policy control.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAccountState extends AccountState
{

  /** password policy specific enum. */
  private final PasswordPolicyControl.Error ppError;


  /**
   * Creates a new password policy account state.
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
   * @param  error  containing password policy error details
   */
  public PasswordPolicyAccountState(final PasswordPolicyControl.Error error)
  {
    super(error);
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
