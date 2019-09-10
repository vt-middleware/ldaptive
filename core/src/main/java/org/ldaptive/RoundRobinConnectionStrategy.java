/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;

/**
 * Connection strategy that moves the first URL in it's list to the end of that list.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{
  @Override
  public LdapURL next(final LdapURLSet urlSet)
  {
    final List<LdapURL> active = urlSet.getActiveUrls();
    if (active.isEmpty()) {
      throw new IllegalStateException("No active LDAP URLs available");
    }
    return active.get(urlSet.getUsageCount() % active.size());
  }
}
