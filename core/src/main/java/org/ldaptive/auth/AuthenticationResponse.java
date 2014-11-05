/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.control.ResponseControl;

/**
 * Response object for authenticator.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AuthenticationResponse extends Response<Boolean>
{

  /** Result of the authentication operation. */
  private final AuthenticationResultCode authenticationResultCode;

  /** Ldap entry of authenticated user. */
  private final LdapEntry ldapEntry;

  /** Account state. */
  private AccountState accountState;


  /**
   * Creates a new authentication response.
   *
   * @param  authRc  authentication result code
   * @param  rc  result code from the underlying ldap operation
   * @param  entry  of the authenticated user
   */
  public AuthenticationResponse(
    final AuthenticationResultCode authRc,
    final ResultCode rc,
    final LdapEntry entry)
  {
    super(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS == authRc,
      rc);
    authenticationResultCode = authRc;
    ldapEntry = entry;
  }


  /**
   * Creates a new authentication response.
   *
   * @param  authRc  authentication result code
   * @param  rc  result code from the underlying ldap operation
   * @param  entry  of the authenticated user
   * @param  msg  authentication message
   */
  public AuthenticationResponse(
    final AuthenticationResultCode authRc,
    final ResultCode rc,
    final LdapEntry entry,
    final String msg)
  {
    super(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS == authRc,
      rc,
      msg,
      null,
      null,
      null,
      -1);
    authenticationResultCode = authRc;
    ldapEntry = entry;
  }


  /**
   * Creates a new authentication response.
   *
   * @param  authRc  authentication result code
   * @param  rc  result code from the underlying ldap operation
   * @param  entry  of the authenticated user
   * @param  msg  authentication message
   * @param  controls  response controls from the underlying ldap operation
   * @param  msgId  message id from the underlying ldap operation
   */
  public AuthenticationResponse(
    final AuthenticationResultCode authRc,
    final ResultCode rc,
    final LdapEntry entry,
    final String msg,
    final ResponseControl[] controls,
    final int msgId)
  {
    super(
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS == authRc,
      rc,
      msg,
      null,
      controls,
      null,
      msgId);
    authenticationResultCode = authRc;
    ldapEntry = entry;
  }


  /**
   * Returns the result code associated with the authentication operation.
   *
   * @return  authentication result code
   */
  public AuthenticationResultCode getAuthenticationResultCode()
  {
    return authenticationResultCode;
  }


  /**
   * Returns the ldap entry of the authenticated user.
   *
   * @return  ldap entry
   */
  public LdapEntry getLdapEntry()
  {
    return ldapEntry;
  }


  /**
   * Returns the account state associated with the authenticated user.
   *
   * @return  account state
   */
  public AccountState getAccountState()
  {
    return accountState;
  }


  /**
   * Sets the account state for the authenticated user.
   *
   * @param  state  for this user
   */
  public void setAccountState(final AccountState state)
  {
    accountState = state;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::authenticationResultCode=%s, ldapEntry=%s, accountState=%s, " +
        "result=%s, resultCode=%s, message=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        authenticationResultCode,
        ldapEntry,
        accountState,
        getResult(),
        getResultCode(),
        getMessage(),
        Arrays.toString(getControls()));
  }
}
