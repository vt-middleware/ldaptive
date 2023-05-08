/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.AbstractResult;
import org.ldaptive.Connection;
import org.ldaptive.LdapUtils;
import org.ldaptive.Result;

/**
 * Response object for authentication handlers.
 *
 * @author  Middleware Services
 */
public class AuthenticationHandlerResponse extends AbstractResult
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10429;

  /** Authentication result code. */
  private AuthenticationResultCode authenticationResultCode;

  /** Connection that authentication occurred on. */
  private Connection connection;


  /** Default constructor. */
  private AuthenticationHandlerResponse() {}


  /**
   * Creates a new authentication response.
   *
   * @param  <T>  type of LDAP result
   * @param  result  of the LDAP operation used to produce this response
   * @param  code  authentication result code
   * @param  conn  connection the authentication occurred on
   */
  public <T extends Result> AuthenticationHandlerResponse(
    final T result,
    final AuthenticationResultCode code,
    final Connection conn)
  {
    copyValues(result);
    authenticationResultCode = code;
    connection = conn;
  }


  public AuthenticationResultCode getAuthenticationResultCode()
  {
    return authenticationResultCode;
  }


  public Connection getConnection()
  {
    return connection;
  }


  @Override
  public boolean isSuccess()
  {
    return AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS == authenticationResultCode;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof AuthenticationHandlerResponse) {
      final AuthenticationHandlerResponse v = (AuthenticationHandlerResponse) o;
      return super.equals(o) &&
             LdapUtils.areEqual(authenticationResultCode, v.authenticationResultCode) &&
             LdapUtils.areEqual(connection, v.connection);
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
      authenticationResultCode,
      connection);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "connection=" + connection + ", " +
      "authenticationResultCode=" + authenticationResultCode + ", " +
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
  protected static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, AuthenticationHandlerResponse>
  {


    protected Builder()
    {
      super(new AuthenticationHandlerResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder resultCode(final AuthenticationResultCode code)
    {
      object.authenticationResultCode = code;
      return this;
    }


    public Builder connection(final Connection conn)
    {
      object.connection = conn;
      return this;
    }
  }
  // CheckStyle:ON
}
