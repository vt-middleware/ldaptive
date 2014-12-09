/* See LICENSE for licensing and NOTICE for copyright. */
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
 */
public class PropsLoginModule extends AbstractLoginModule
{

  /** Authenticator to load properties for. */
  private Authenticator auth;

  /** Authentication request to load properties for. */
  private AuthenticationRequest authRequest;

  /** Role resolver to load properties for. */
  private RoleResolver roleResolver;

  /** Search request to load properties for. */
  private SearchRequest searchRequest;


  @Override
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);
    AuthenticatorFactory authenticatorFactory = new PropertiesAuthenticatorFactory();
    auth = authenticatorFactory.createAuthenticator(options);
    authRequest = authenticatorFactory.createAuthenticationRequest(options);
    RoleResolverFactory roleResolverFactory = new PropertiesRoleResolverFactory();
    roleResolver = roleResolverFactory.createRoleResolver(options);
    searchRequest = roleResolverFactory.createSearchRequest(options);
  }


  @Override
  protected boolean login(
    final NameCallback nameCb,
    final PasswordCallback passCb)
    throws LoginException
  {
    return true;
  }


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


  @Override
  public boolean abort()
  {
    loginSuccess = false;
    return true;
  }


  @Override
  public boolean logout()
  {
    return true;
  }
}
