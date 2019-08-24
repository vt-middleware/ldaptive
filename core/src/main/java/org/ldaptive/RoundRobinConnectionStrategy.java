/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Connection strategy that moves the first URL in it's list to the end of that list.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{


  public RoundRobinConnectionStrategy()
  {
    active = new LinkedHashMap<>();
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
      if (inactive.size() > 0) {
        l.addAll(inactive.values().stream().map(Map.Entry::getValue).collect(Collectors.toList()));
      }

      final Map.Entry<Integer, LdapURL> entry = active.entrySet().iterator().next();
      active.remove(entry.getKey());
      active.put(entry.getKey(), entry.getValue());
      return Collections.unmodifiableList(l);
    }
  }
}
