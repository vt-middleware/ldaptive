/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Returns an LDAP entry that contains only the DN that was supplied to it.
 *
 * @author  Middleware Services
 */
public final class NoOpEntryResolver implements EntryResolver
{


  @Override
  public LdapEntry resolve(final AuthenticationCriteria criteria, final AuthenticationHandlerResponse response)
  {
    LdapUtils.assertNotNullArg(criteria, "Authentication criteria cannot be null");
    LdapUtils.assertNotNullArg(response, "Authentication response cannot be null");
    return LdapEntry.builder().dn(criteria.getDn()).freeze().build();
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "]";
  }
}
