/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.List;

/**
 * Connection strategy that moves the first URL in it's list to the end of that list.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{


  public RoundRobinConnectionStrategy()
  {
    super(LdapURLSet.Type.ORDERED);
  }


  @Override
  public List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    final List<LdapURL> urls = ldapURLSet.getUrls(null);
    ldapURLSet.firstToLast();
    return Collections.unmodifiableList(urls);
  }
}
