/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Connection strategy that randomizes the list of configured URLs. A random URL ordering will be created for each
 * connection attempt.
 *
 * @author  Middleware Services
 */
public class RandomConnectionStrategy extends AbstractConnectionStrategy
{


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    // CheckStyle:AnonInnerLength OFF
    return new Iterator<>() {
      private final List<LdapURL> active = ldapURLSet.getActiveUrls().stream().collect(
        Collectors.collectingAndThen(
          Collectors.toCollection(ArrayList::new),
          list -> {
            Collections.shuffle(list);
            return list;
          }));
      private final List<LdapURL> inactive = ldapURLSet.getInactiveUrls().stream().collect(
        Collectors.collectingAndThen(
          Collectors.toCollection(ArrayList::new),
          list -> {
            Collections.shuffle(list);
            return list;
          }));
      private int i;


      @Override
      public boolean hasNext()
      {
        return i < active.size() + inactive.size();
      }


      @Override
      public LdapURL next()
      {
        final LdapURL url;
        if (i < active.size()) {
          url = active.get(i);
        } else {
          url = inactive.get(i - active.size());
        }
        i++;
        return url;
      }
    };
    // CheckStyle:AnonInnerLength ON
  }
}
