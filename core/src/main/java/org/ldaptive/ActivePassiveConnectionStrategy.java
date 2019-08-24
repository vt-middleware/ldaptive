/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    active = new TreeMap<>();
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
      return Collections.unmodifiableList(l);
    }
  }
}
