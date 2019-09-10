/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;

/**
 * Connection strategy that attempts hosts ordered exactly the way they are configured. This means that the first host
 * will always be attempted first, followed by each host in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy extends AbstractConnectionStrategy
{
  @Override
  public LdapURL next(final LdapURLSet urlSet)
  {
    final List<LdapURL> active = urlSet.getActiveUrls();
    if (active.isEmpty()) {
      throw new IllegalStateException("No active LDAP URLs available");
    }
    return active.get(0);
  }
}
