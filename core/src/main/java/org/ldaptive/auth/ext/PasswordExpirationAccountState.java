/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;
import org.ldaptive.auth.AccountState;

/**
 * Represents the state of an account in a directory that implements:
 * http://tools.ietf.org/html/draft-vchu-ldap-pwd-policy-00. Note that the warning returned by this implementation
 * always returns -1 for logins remaining as this specification doesn't include that feature.
 *
 * @author  Middleware Services
 */
public class PasswordExpirationAccountState extends AccountState
{


  /** Enum to define password expiration error. */
  public enum Error implements AccountState.Error {

    /** password expired. */
    PASSWORD_EXPIRED;


    @Override
    public int getCode()
    {
      return 0;
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
      throw new CredentialExpiredException(name());
    }
  }

  /** error enum. */
  private final Error nError;


  /**
   * Creates a new password expiration account state.
   *
   * @param  exp  account expiration
   */
  public PasswordExpirationAccountState(final ZonedDateTime exp)
  {
    super(new AccountState.DefaultWarning(exp, -1));
    nError = null;
  }


  /**
   * Creates a new password expiration account state.
   *
   * @param  error  containing authentication failure details
   */
  public PasswordExpirationAccountState(final PasswordExpirationAccountState.Error error)
  {
    super(error);
    nError = error;
  }


  /**
   * Returns the password expiration error for this account state.
   *
   * @return  password expiration error
   */
  public PasswordExpirationAccountState.Error getPasswordExpirationError()
  {
    return nError;
  }
}
