/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides pre authentication handling of authentication requests.
 *
 * @author  Middleware Services
 */
public interface AuthenticationRequestHandler
{


  /**
   * Handle the request for an ldap authentication.
   *
   * @param  dn  distinguished name resolved for this request
   * @param  request  for this authentication event
   *
   * @throws  LdapException  if an error occurs handling an authentication request
   */
  void handle(String dn, AuthenticationRequest request)
    throws LdapException;
}
