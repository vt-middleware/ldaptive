/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Connection strategy that reorders it's URLs based on the number of times it's been invoked.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy extends AbstractConnectionStrategy
{

  /** Usage counter. */
  private final AtomicInteger counter = new AtomicInteger();

  /** Custom iterator function. */
  private final Function<List<LdapURL>, Iterator<LdapURL>> iterFunction;


  /** Default constructor. */
  public RoundRobinConnectionStrategy()
  {
    this(null);
  }


  /**
   * Creates a new round robin connection strategy.
   *
   * @param  function  that produces a custom iterator
   */
  public RoundRobinConnectionStrategy(final Function<List<LdapURL>, Iterator<LdapURL>> function)
  {
    iterFunction = function;
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
    if (iterFunction != null) {
      return iterFunction.apply(ldapURLSet.getUrls());
    }
    return new DefaultLdapURLIterator(urls);
  }
}
