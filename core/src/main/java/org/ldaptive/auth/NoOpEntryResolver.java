/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

/**
 * Returns an LDAP entry that contains only the DN that was supplied to it.
 *
 * @author  Middleware Services
 */
public class NoOpEntryResolver implements EntryResolver
{


  /** {@inheritDoc} */
  @Override
  public LdapEntry resolve(
    final Connection conn,
    final AuthenticationCriteria ac)
    throws LdapException
  {
    return new LdapEntry(ac.getDn());
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
