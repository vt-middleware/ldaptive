/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides an interface for finding LDAP DNs with a user identifier.
 *
 * @author  Middleware Services
 */
public interface DnResolver
{


  /**
   * Attempts to find the LDAP DN for the supplied user.
   *
   * @param  user  to find DN for
   *
   * @return  user DN
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  String resolve(String user)
    throws LdapException;
}
