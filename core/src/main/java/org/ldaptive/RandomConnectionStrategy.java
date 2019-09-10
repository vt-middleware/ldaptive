/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import java.util.Random;

/**
 * Connection strategy that randomizes the list of configured URLs. A random URL ordering will be created for each
 * connection attempt.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategy extends AbstractConnectionStrategy
{
  /** Random number generator. */
  private final Random rnd = new Random();

  @Override
  public LdapURL next(final LdapURLSet urlSet)
  {
    final List<LdapURL> active = urlSet.getActiveUrls();
    if (active.isEmpty()) {
      throw new IllegalStateException("No active LDAP URLs available");
    }
    return active.get(rnd.nextInt(active.size()));
  }
}
