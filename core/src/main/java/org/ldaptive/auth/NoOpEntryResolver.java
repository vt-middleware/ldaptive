/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapEntry;

/**
 * Returns an LDAP entry that contains only the DN that was supplied to it.
 *
 * @author  Middleware Services
 */
public class NoOpEntryResolver implements EntryResolver
{


  @Override
  public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
  {
    return LdapEntry.builder().dn(criteria.getDn()).build();
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(getClass().getName()).append("@").append(hashCode()).append("]").toString();
  }
}
