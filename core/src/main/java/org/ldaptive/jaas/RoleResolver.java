/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Set;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;

/**
 * Looks up a user's roles using an LDAP search.
 *
 * @author  Middleware Services
 */
public interface RoleResolver
{


  /**
   * Executes a search request and converts any attributes to ldap roles.
   *
   * @param  request  to execute
   *
   * @return  ldap roles
   *
   * @throws  LdapException  if the ldap operation fails
   */
  Set<LdapRole> search(SearchRequest request)
    throws LdapException;
}
