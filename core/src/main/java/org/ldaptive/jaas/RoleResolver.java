/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Set;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;

/**
 * Looks up a user's roles using an LDAP search.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
