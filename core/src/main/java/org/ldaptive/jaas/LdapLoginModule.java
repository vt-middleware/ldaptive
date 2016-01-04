/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import com.sun.security.auth.callback.TextCallbackHandler;
import org.ldaptive.Credential;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.User;

/**
 * Provides a JAAS authentication hook for LDAP authentication.
 *
 * @author  Middleware Services
 */
public class LdapLoginModule extends AbstractLoginModule
{

  /** User attribute to add to role data. */
  private String[] userRoleAttribute = ReturnAttributes.NONE.value();

  /** Factory for creating authenticators with JAAS options. */
  private AuthenticatorFactory authenticatorFactory;

  /** Authenticator to use against the LDAP. */
  private Authenticator auth;

  /** Authentication request to use for authentication. */
  private AuthenticationRequest authRequest;


  @Override
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    setLdapPrincipal = true;
    setLdapCredential = true;

    super.initialize(subject, callbackHandler, sharedState, options);

    for (String key : options.keySet()) {
      final String value = (String) options.get(key);
      if ("userRoleAttribute".equalsIgnoreCase(key)) {
        if ("".equals(value)) {
          userRoleAttribute = ReturnAttributes.NONE.value();
        } else if ("*".equals(value)) {
          userRoleAttribute = ReturnAttributes.ALL_USER.value();
        } else {
          userRoleAttribute = value.split(",");
        }
      } else if ("authenticatorFactory".equalsIgnoreCase(key)) {
        try {
          authenticatorFactory = (AuthenticatorFactory) Class.forName(value).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
          throw new IllegalArgumentException(e);
        }
      }
    }

    if (authenticatorFactory == null) {
      authenticatorFactory = new PropertiesAuthenticatorFactory();
    }

    logger.trace(
      "authenticatorFactory = {}, userRoleAttribute = {}",
      authenticatorFactory,
      Arrays.toString(userRoleAttribute));

    auth = authenticatorFactory.createAuthenticator(options);
    logger.debug("Retrieved authenticator from factory: {}", auth);

    authRequest = authenticatorFactory.createAuthenticationRequest(options);
    authRequest.setReturnAttributes(userRoleAttribute);
    logger.debug("Retrieved authentication request from factory: {}", authRequest);
  }


  @Override
  protected boolean login(final NameCallback nameCb, final PasswordCallback passCb)
    throws LoginException
  {
    try {
      getCredentials(nameCb, passCb, false);
      authRequest.setUser(new User(nameCb.getName()));
      authRequest.setCredential(new Credential(passCb.getPassword()));

      AuthenticationResponse response = auth.authenticate(authRequest);
      LdapEntry entry = null;
      if (response.getResult()) {
        entry = response.getLdapEntry();
        if (entry != null) {
          roles.addAll(LdapRole.toRoles(entry));
          if (defaultRole != null && !defaultRole.isEmpty()) {
            roles.addAll(defaultRole);
          }
        }
        loginSuccess = true;
      } else {
        if (tryFirstPass) {
          getCredentials(nameCb, passCb, true);
          response = auth.authenticate(authRequest);
          if (response.getResult()) {
            entry = response.getLdapEntry();
            if (entry != null) {
              roles.addAll(LdapRole.toRoles(entry));
            }
            if (defaultRole != null && !defaultRole.isEmpty()) {
              roles.addAll(defaultRole);
            }
            loginSuccess = true;
          } else {
            loginSuccess = false;
          }
        } else {
          loginSuccess = false;
        }
      }

      if (!loginSuccess) {
        logger.debug("Authentication failed: " + response);
        throw new LoginException("Authentication failed: " + response);
      } else {
        if (setLdapPrincipal) {
          principals.add(new LdapPrincipal(nameCb.getName(), entry));
        }

        final String loginDn = response.getResolvedDn();
        if (loginDn != null && setLdapDnPrincipal) {
          principals.add(new LdapDnPrincipal(loginDn, entry));
        }

        if (setLdapCredential) {
          credentials.add(new LdapCredential(passCb.getPassword()));
        }
        storeCredentials(nameCb, passCb, loginDn);
      }
    } catch (LdapException e) {
      logger.debug("Error occurred attempting authentication", e);
      loginSuccess = false;
      throw new LoginException(e != null ? e.getMessage() : "Authentication Error");
    }
    return true;
  }


  /**
   * This provides command line access to this JAAS module.
   *
   * @param  args  command line arguments
   *
   * @throws  Exception  if an error occurs
   */
  public static void main(final String[] args)
    throws Exception
  {
    String name = "ldaptive";
    if (args.length > 0) {
      name = args[0];
    }

    final LoginContext lc = new LoginContext(name, new TextCallbackHandler());
    lc.login();
    System.out.println("Authentication/Authorization succeeded");

    final Set<Principal> principals = lc.getSubject().getPrincipals();
    System.out.println("Subject Principal(s): ");

    for (Principal p : principals) {
      System.out.println("  " + p);
    }
    lc.logout();
  }
}
