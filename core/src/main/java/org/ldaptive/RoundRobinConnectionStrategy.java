/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection strategy that reorders it's URLs based on the number of times it's been invoked.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{

  /** Usage counter. */
  private final AtomicInteger counter = new AtomicInteger();

  /** Whether to return a circular iterator. */
  private final boolean circularIter;


  /** Default constructor. */
  public RoundRobinConnectionStrategy()
  {
    this(false);
  }


  /**
   * Creates a new round robin connection strategy.
   *
   * @param  circular  use a circular iterator
   */
  public RoundRobinConnectionStrategy(final boolean circular)
  {
    circularIter = circular;
  }


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
    return new DefaultLdapURLIterator(urls, circularIter);
  }
}
