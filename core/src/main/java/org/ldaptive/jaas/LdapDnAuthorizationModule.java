/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import com.sun.security.auth.callback.TextCallbackHandler;
import org.ldaptive.LdapException;
import org.ldaptive.auth.Authenticator;

/**
 * Provides a JAAS authentication hook into LDAP DNs. No authentication is performed by this module. The LDAP entry DN
 * can be stored and shared with other JAAS modules.
 *
 * @author  Middleware Services
 */
public class LdapDnAuthorizationModule extends AbstractLoginModule
{

  /** Whether failing to find a DN should raise an exception. */
  private boolean noResultsIsError;

  /** Factory for creating authenticators with JAAS options. */
  private AuthenticatorFactory authenticatorFactory;

  /** Authenticator to use against the LDAP. */
  private Authenticator auth;


  @Override
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    for (String key : options.keySet()) {
      final String value = (String) options.get(key);
      if ("noResultsIsError".equalsIgnoreCase(key)) {
        noResultsIsError = Boolean.valueOf(value);
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

    logger.trace("authenticatorFactory = {}, noResultsIsError = {}", authenticatorFactory, noResultsIsError);

    auth = authenticatorFactory.createAuthenticator(options);
    logger.debug("Retrieved authenticator from factory: {}", auth);
  }


  @Override
  protected boolean login(final NameCallback nameCb, final PasswordCallback passCb)
    throws LoginException
  {
    try {
      getCredentials(nameCb, passCb, false);

      if (nameCb.getName() == null && tryFirstPass) {
        getCredentials(nameCb, passCb, true);
      }

      final String loginName = nameCb.getName();
      if (loginName != null && setLdapPrincipal) {
        principals.add(new LdapPrincipal(loginName, null));
        loginSuccess = true;
      }

      final String loginDn = auth.resolveDn(nameCb.getName());
      if (loginDn == null && noResultsIsError) {
        loginSuccess = false;
        throw new LoginException("Could not find DN for " + nameCb.getName());
      }
      if (loginDn != null && setLdapDnPrincipal) {
        principals.add(new LdapDnPrincipal(loginDn, null));
        loginSuccess = true;
      }
      if (defaultRole != null && !defaultRole.isEmpty()) {
        roles.addAll(defaultRole);
        loginSuccess = true;
      }
      storeCredentials(nameCb, passCb, loginDn);
    } catch (LdapException e) {
      logger.debug("Error occurred attempting DN lookup", e);
      loginSuccess = false;
      throw new LoginException(e != null ? e.getMessage() : "DN resolution error");
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
    String name = "ldaptive-dn";
    if (args.length > 0) {
      name = args[0];
    }

    final LoginContext lc = new LoginContext(name, new TextCallbackHandler());
    lc.login();

    final Set<Principal> principals = lc.getSubject().getPrincipals();
    System.out.println("Subject Principal(s): ");

    for (Principal p : principals) {
      System.out.println("  " + p.getName());
    }
    lc.logout();
  }
}
