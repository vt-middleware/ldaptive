/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

/**
 * Returns an LDAP entry that contains only the DN that was supplied to it.
 *
 * @author  Middleware Services
 */
public class NoOpEntryResolver implements EntryResolver
{


  @Override
  public LdapEntry resolve(
    final AuthenticationCriteria criteria,
    final AuthenticationHandlerResponse response)
    throws LdapException
  {
    return new LdapEntry(criteria.getDn());
  }


  @Override
  public String toString()
  {
    return String.format("[%s@%d]", getClass().getName(), hashCode());
  }
}
