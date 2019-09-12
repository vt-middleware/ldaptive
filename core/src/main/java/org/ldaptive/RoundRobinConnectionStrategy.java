/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection strategy that moves the first URL in it's list to the end of that list.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{

  /** Usage counter. */
  private final AtomicInteger counter = new AtomicInteger();


  @Override
  public synchronized Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    final List<LdapURL> urls = new ArrayList<>(ldapURLSet.getActiveUrls());
    for (int i = 0; i < counter.get(); i++) {
      urls.add(urls.remove(0));
    }
    urls.addAll(ldapURLSet.getInactiveUrls());
    counter.incrementAndGet();
    return new Iterator<>() {
      private int i;


      @Override
      public boolean hasNext()
      {
        return i < urls.size();
      }


      @Override
      public LdapURL next()
      {
        return urls.get(i++);
      }
    };
  }
}
