/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

/**
 * Provides an interface for finding a user's ldap entry after a successful authentication.
 *
 * @author  Middleware Services
 */
public interface EntryResolver
{


  /**
   * Attempts to find the LDAP entry for the supplied authentication criteria and authentication handler response. The
   * connection available in the response should <b>not</b> be closed in this method.
   *
   * @param  criteria  authentication criteria used to perform the authentication
   * @param  response  produced by the authentication handler
   *
   * @return  ldap entry
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  LdapEntry resolve(AuthenticationCriteria criteria, AuthenticationHandlerResponse response)
    throws LdapException;
}
