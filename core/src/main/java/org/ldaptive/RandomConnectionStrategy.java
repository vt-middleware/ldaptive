/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    active = new HashMap<>();
  }


  @Override
  public List<LdapURL> apply()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    synchronized (lock) {
      final List<LdapURL> l = new ArrayList<>();
      l.addAll(active.values());
      Collections.shuffle(l);
      if (inactive.size() > 0) {
        l.addAll(inactive.values().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
      }
      return Collections.unmodifiableList(l);
    }
  }
}
