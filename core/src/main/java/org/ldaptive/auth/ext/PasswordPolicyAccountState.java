/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.auth.AccountState;
import org.ldaptive.control.PasswordPolicyControl;

/**
 * Represents the state of an account as described by a password policy control.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
  public PasswordPolicyAccountState(final Calendar exp, final int remaining)
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
