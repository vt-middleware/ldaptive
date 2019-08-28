/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collections;
import java.util.List;

/**
 * Connection strategy that randomizes the list of configured URLs. A random URL ordering will be created for each
 * connection attempt.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategy extends AbstractConnectionStrategy
{


  /** Default constructor. */
  public RandomConnectionStrategy()
  {
    super(LdapURLSet.Type.UNORDERED);
  }


  @Override
  public List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return ldapURLSet.getUrls(urls -> Collections.shuffle(urls));
  }
}
