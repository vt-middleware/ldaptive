/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Connection strategy that attempts hosts ordered exactly the way they are configured. This means that the first host
 * will always be attempted first, followed by each host in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy extends AbstractConnectionStrategy
{

  /** Custom iterator function. */
  private final Function<List<LdapURL>, Iterator<LdapURL>> iterFunction;


  /** Default constructor. */
  public ActivePassiveConnectionStrategy()
  {
    this(null);
  }


  /**
   * Creates a new active passive connection strategy.
   *
   * @param  function  that produces a custom iterator
   */
  public ActivePassiveConnectionStrategy(final Function<List<LdapURL>, Iterator<LdapURL>> function)
  {
    iterFunction = function;
  }


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    if (iterFunction != null) {
      return iterFunction.apply(ldapURLSet.getUrls());
    }
    return new DefaultLdapURLIterator(ldapURLSet.getUrls());
  }
}
