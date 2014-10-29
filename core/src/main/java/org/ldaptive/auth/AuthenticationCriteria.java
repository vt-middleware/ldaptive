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
package org.ldaptive.auth;

import org.ldaptive.Credential;

/**
 * Contains the properties used to perform authentication.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
  public AuthenticationCriteria(
    final String dn,
    final AuthenticationRequest request)
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
   * Sets the credential.
   *
   * @param  c  to set credential
   *
   * @deprecated  use {@link #setAuthenticationRequest(AuthenticationRequest)}
   * instead
   */
  @Deprecated
  public void setCredential(final Credential c)
  {
    authenticationRequest.setCredential(c);
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


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::dn=%s, authenticationRequest=%s]",
        getClass().getName(),
        hashCode(),
        authenticationDn,
        authenticationRequest);
  }
}
