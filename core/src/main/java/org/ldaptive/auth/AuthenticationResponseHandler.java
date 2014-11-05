/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides post authentication handling of authentication responses.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface AuthenticationResponseHandler
{


  /**
   * Handle the response from an ldap authentication.
   *
   * @param  response  produced from an authentication
   *
   * @throws  LdapException  if an error occurs handling an authentication
   * response
   */
  void handle(AuthenticationResponse response)
    throws LdapException;
}
