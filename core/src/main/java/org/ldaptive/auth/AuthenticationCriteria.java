/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.Credential;

/**
 * Contains the properties used to perform authentication.
 *
 * @author  Middleware Services
 */
public class AuthenticationCriteria
{

  /** dn. */
  private String authenticationDn;

  /** authentication request. */
  private AuthenticationRequest authenticationRequest;


  /** Default constructor. */
  public AuthenticationCriteria() {}


  /**
   * Creates a new authentication criteria.
   *
   * @param  dn  to authenticate
   */
  public AuthenticationCriteria(final String dn)
  {
    authenticationDn = dn;
  }


  /**
   * Creates a new authentication criteria.
   *
   * @param  dn  to authenticate
   * @param  request  that initiated the authentication
   */
  public AuthenticationCriteria(final String dn, final AuthenticationRequest request)
  {
    authenticationDn = dn;
    authenticationRequest = request;
  }


  /**
   * Returns the dn.
   *
   * @return  dn to authenticate
   */
  public String getDn()
  {
    return authenticationDn;
  }


  /**
   * Sets the dn.
   *
   * @param  dn  to set dn
   */
  public void setDn(final String dn)
  {
    authenticationDn = dn;
  }


  /**
   * Returns the credential.
   *
   * @return  credential to authenticate dn
   */
  public Credential getCredential()
  {
    return authenticationRequest.getCredential();
  }


  /**
   * Returns the authentication request.
   *
   * @return  authentication request
   */
  public AuthenticationRequest getAuthenticationRequest()
  {
    return authenticationRequest;
  }


  /**
   * Sets the authentication request.
   *
   * @param  request  to set authentication request
   */
  public void setAuthenticationRequest(final AuthenticationRequest request)
  {
    authenticationRequest = request;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("dn=").append(authenticationDn).append(", ")
      .append("authenticationRequest=").append(authenticationRequest).append("]").toString();
  }
}
