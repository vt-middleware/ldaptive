/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides an interface for LDAP authentication implementations.
 *
 * @author  Middleware Services
 */
public interface AuthenticationHandler
{


  /**
   * Perform an ldap authentication.
   *
   * @param  criteria  to perform the authentication with
   *
   * @return  authentication handler response
   *
   * @throws  LdapException  if ldap operation fails
   */
  AuthenticationHandlerResponse authenticate(AuthenticationCriteria criteria)
    throws LdapException;
}
