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
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchRequest;

/**
 * Provides a JAAS authentication hook into LDAP roles. No authentication is performed in this module. Role data is set
 * for the login name in the shared state or for the name returned by the CallbackHandler.
 *
 * @author  Middleware Services
 */
public class LdapRoleAuthorizationModule extends AbstractLoginModule
{

  /** Ldap filter for role searches. */
  private String roleFilter;

  /** Role attribute to add to role data. */
  private String[] roleAttribute = ReturnAttributes.NONE.value();

  /** Whether failing to find any roles should raise an exception. */
  private boolean noResultsIsError;

  /** Factory for creating role resolvers with JAAS options. */
  private RoleResolverFactory roleResolverFactory;

  /** To search for roles. */
  private RoleResolver roleResolver;

  /** Search request to use for roles. */
  private SearchRequest searchRequest;


  @Override
  public void initialize(
    final Subject subject,
    final CallbackHandler callbackHandler,
    final Map<String, ?> sharedState,
    final Map<String, ?> options)
  {
    super.initialize(subject, callbackHandler, sharedState, options);

    for (Map.Entry<String, ?> entry : options.entrySet()) {
      final String key = entry.getKey();
      final String value = (String) entry.getValue();
      if ("roleFilter".equalsIgnoreCase(key)) {
        roleFilter = value;
      } else if ("roleAttribute".equalsIgnoreCase(key)) {
        if ("".equals(value)) {
          roleAttribute = ReturnAttributes.NONE.value();
        } else if ("*".equals(value)) {
          roleAttribute = ReturnAttributes.ALL_USER.value();
        } else {
          roleAttribute = value.split(",");
        }
      } else if ("noResultsIsError".equalsIgnoreCase(key)) {
        noResultsIsError = Boolean.parseBoolean(value);
      } else if ("roleResolverFactory".equalsIgnoreCase(key)) {
        try {
          roleResolverFactory = (RoleResolverFactory) Class.forName(value).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
    }

    if (roleResolverFactory == null) {
      roleResolverFactory = new PropertiesRoleResolverFactory();
    }

    logger.trace(
      "roleResolverFactory = {}, roleFilter = {}, roleAttribute = {}, noResultsIsError = {}",
      roleResolverFactory,
      roleFilter,
      Arrays.toString(roleAttribute),
      noResultsIsError);

    roleResolver = roleResolverFactory.createRoleResolver(options);
    logger.debug("Retrieved role resolver from factory: {}", roleResolver);

    searchRequest = roleResolverFactory.createSearchRequest(options);
    searchRequest.setReturnAttributes(roleAttribute);
    logger.debug("Retrieved search request from factory: {}", searchRequest);
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

      final String loginDn = (String) sharedState.get(LOGIN_DN);
      if (loginDn != null && setLdapDnPrincipal) {
        principals.add(new LdapDnPrincipal(loginDn, null));
        loginSuccess = true;
      }

      final FilterTemplate template = new FilterTemplate(roleFilter);
      template.setParameter("dn", loginDn);
      template.setParameter("user", loginName);
      searchRequest.setFilter(template);

      final Set<LdapRole> lr = roleResolver.search(searchRequest);
      if (lr.isEmpty() && noResultsIsError) {
        loginSuccess = false;
        throw new LoginException("Could not find roles using " + roleFilter);
      }
      roles.addAll(lr);
      if (defaultRole != null && !defaultRole.isEmpty()) {
        roles.addAll(defaultRole);
      }
      if (!roles.isEmpty()) {
        loginSuccess = true;
      }
      storeCredentials(nameCb, passCb, null);
    } catch (LdapException e) {
      logger.debug("Error occurred attempting role lookup", e);
      loginSuccess = false;
      throw new LoginException(e.getMessage());
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
    String name = "ldaptive-role";
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
