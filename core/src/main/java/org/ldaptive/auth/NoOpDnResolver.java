/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Returns a DN that is the user identifier.
 *
 * @author  Middleware Services
 */
public final class NoOpDnResolver implements DnResolver
{


  /**
   * Returns the user as the DN.
   *
   * @param  user  to set as DN
   *
   * @return  user as DN
   *
   * @throws  LdapException  never
   */
  @Override
  public String resolve(final User user)
    throws LdapException
  {
    return user != null ? user.getIdentifier() : null;
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "]";
  }
}
