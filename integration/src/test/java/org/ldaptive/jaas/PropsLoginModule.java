/*
  $Id: PropsLoginModule.java 2909 2014-03-17 19:28:44Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2909 $
  Updated: $Date: 2014-03-17 15:28:44 -0400 (Mon, 17 Mar 2014) $
*/
package org.ldaptive.jaas;

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import org.ldaptive.SearchRequest;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;

/**
 * Login module for testing configuration properties.
 *
 * @author  Middleware Services
 * @version  $Revision: 2909 $ $Date: 2014-03-17 15:28:44 -0400 (Mon, 17 Mar 2014) $
 */
public class PropsLoginModule extends AbstractLoginModule
{

  /** Factory for creating authenticators with JAAS options. */
  private AuthenticatorFactory authenticatorFactory;

  /** Factory for creating role resolvers with JAAS options. */
  private RoleResolverFactory roleResolverFactory;

  /** Authenticator to load properties for. */
  private Authenticator auth;

  /** Authentication request to load properties for. */
  private AuthenticationRequest authRequest;

  /** Role resolver to load properties for. */
  private RoleResolver roleResolver;

  /** Search request to load properties for. */
  private SearchRequest searchRequest;


  /** {@inheritDoc} */
  @Override
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);
    authenticatorFactory = new PropertiesAuthenticatorFactory();
    auth = authenticatorFactory.createAuthenticator(options);
    authRequest = authenticatorFactory.createAuthenticationRequest(options);
    roleResolverFactory = new PropertiesRoleResolverFactory();
    roleResolver = roleResolverFactory.createRoleResolver(options);
    searchRequest = roleResolverFactory.createSearchRequest(options);
  }


  /** {@inheritDoc} */
  @Override
  protected boolean login(
    final NameCallback nameCb,
    final PasswordCallback passCb)
    throws LoginException
  {
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public boolean commit()
    throws LoginException
  {
    subject.getPublicCredentials().add(auth);
    subject.getPublicCredentials().add(authRequest);
    subject.getPublicCredentials().add(roleResolver);
    subject.getPublicCredentials().add(searchRequest);
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public boolean abort()
  {
    loginSuccess = false;
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public boolean logout()
  {
    return true;
  }
}
