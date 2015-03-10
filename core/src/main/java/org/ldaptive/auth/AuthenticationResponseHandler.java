/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides post authentication handling of authentication responses.
 *
 * @author  Middleware Services
 */
public interface AuthenticationResponseHandler
{


  /**
   * Handle the response from an ldap authentication.
   *
   * @param  response  produced from an authentication
   *
   * @throws  LdapException  if an error occurs handling an authentication response
   */
  void handle(AuthenticationResponse response)
    throws LdapException;
}
