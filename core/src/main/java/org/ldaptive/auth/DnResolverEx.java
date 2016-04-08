/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Transitional interface to support resolving arbitrary user types.
 *
 * @author  Middleware Services
 */
public interface DnResolverEx
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
  String resolve(User user)
    throws LdapException;
}
