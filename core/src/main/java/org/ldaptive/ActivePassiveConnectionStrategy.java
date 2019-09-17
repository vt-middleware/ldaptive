/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Iterator;

/**
 * Connection strategy that attempts hosts ordered exactly the way they are configured. This means that the first host
 * will always be attempted first, followed by each host in the list.
 *
 * @author  Middleware Services
 */
public class ActivePassiveConnectionStrategy extends AbstractConnectionStrategy
{

  /** Whether to return a circular iterator. */
  private final boolean circularIter;


  /** Default constructor. */
  public ActivePassiveConnectionStrategy()
  {
    this(false);
  }


  /**
   * Creates a new active passive connection strategy.
   *
   * @param  circular  use a circular iterator
   */
  public ActivePassiveConnectionStrategy(final boolean circular)
  {
    circularIter = circular;
  }


  @Override
  public Iterator<LdapURL> iterator()
  {
    if (!isInitialized()) {
      throw new IllegalStateException("Strategy is not initialized");
    }
    return new DefaultLdapURLIterator(ldapURLSet.getUrls(), circularIter);
  }
}
