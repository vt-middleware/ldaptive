/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.AbstractResult;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Synthetic response object that encapsulates data used for authentication.
 *
 * @author  Middleware Services
 */
public final class AuthenticationResponse extends AbstractResult
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10427;

  /** Result of the authentication operation. */
  private AuthenticationHandlerResponse authenticationHandlerResponse;

  /** Resolved DN. */
  private String resolvedDn;

  /** Ldap entry of authenticated user. */
  private LdapEntry ldapEntry;

  /** Account state. */
  private AccountState accountState;


  /** Default constructor. */
  private AuthenticationResponse() {}


  /**
   * Creates a new authentication response.
   *
   * @param  response  authentication handler response
   * @param  dn  produced by the DN resolver
   * @param  entry  of the authenticated user
   */
  public AuthenticationResponse(
    final AuthenticationHandlerResponse response,
    final String dn,
    final LdapEntry entry)
  {
    copyValues(response);
    authenticationHandlerResponse = response;
    resolvedDn = dn;
    ldapEntry = entry;
    ldapEntry.freeze();
    freezeOnConstruct();
  }


  /**
   * Returns whether the authentication handler produced a {@link
   * AuthenticationResultCode#AUTHENTICATION_HANDLER_SUCCESS} result.
   *
   * @return  whether authentication was successful
   */
  public boolean isSuccess()
  {
    return
      AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS ==
        authenticationHandlerResponse.getAuthenticationResultCode();
  }


  public AuthenticationResultCode getAuthenticationResultCode()
  {
    return authenticationHandlerResponse.getAuthenticationResultCode();
  }


  public AuthenticationHandlerResponse getAuthenticationHandlerResponse()
  {
    return authenticationHandlerResponse;
  }


  /**
   * Returns the DN that was resolved in order to perform authentication.
   *
   * @return  resolved dn
   */
  public String getResolvedDn()
  {
    return resolvedDn;
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
    // account state must remain mutable for response handlers
    accountState = state;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AuthenticationResponse) {
      final AuthenticationResponse v = (AuthenticationResponse) o;
      return super.equals(o) &&
             LdapUtils.areEqual(authenticationHandlerResponse, v.authenticationHandlerResponse) &&
             LdapUtils.areEqual(resolvedDn, v.resolvedDn) &&
             LdapUtils.areEqual(ldapEntry, v.ldapEntry) &&
             LdapUtils.areEqual(accountState, v.accountState);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(
      HASH_CODE_SEED,
      getMessageID(),
      getControls(),
      getResultCode(),
      getMatchedDN(),
      getDiagnosticMessage(),
      getReferralURLs(),
      authenticationHandlerResponse,
      resolvedDn,
      ldapEntry,
      accountState);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "authenticationHandlerResponse=" + authenticationHandlerResponse + ", " +
      "resolvedDn=" + resolvedDn + ", " +
      "ldapEntry=" + ldapEntry + ", " +
      "accountState=" + accountState + ", " +
      "resultCode=" + getResultCode() + ", " +
      "matchedDN=" + getMatchedDN() + ", " +
      "diagnosticMessage=" + getEncodedDiagnosticMessage() + ", " +
      "referralURLs=" + Arrays.toString(getReferralURLs()) + ", " +
      "messageID=" + getMessageID() + ", " +
      "controls=" + Arrays.toString(getControls()) + "]";
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  protected static final class Builder extends AbstractResult.AbstractBuilder<Builder, AuthenticationResponse>
  {


    private Builder()
    {
      super(new AuthenticationResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder response(final AuthenticationHandlerResponse response)
    {
      object.copyValues(response);
      object.authenticationHandlerResponse = response;
      return this;
    }


    public Builder dn(final String dn)
    {
      object.resolvedDn = dn;
      return this;
    }


    public Builder entry(final LdapEntry entry)
    {
      object.ldapEntry = entry;
      return this;
    }


    public Builder state(final AccountState state)
    {
      object.accountState = state;
      return this;
    }
  }
  // CheckStyle:ON
}
