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


  public ActivePassiveConnectionStrategy()
  {
    super(LdapURLSet.Type.SORTED);
  }


  @Override
  public List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return ldapURLSet.getUrls(null);
  }
}
