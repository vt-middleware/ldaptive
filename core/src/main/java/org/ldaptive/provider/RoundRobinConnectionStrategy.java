/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Connection strategy that attempts each URL in the order they are configured. The next attempt uses the next URL in
 * the list. The class orders the URLs by computing a modulus with the connection count. Since metadata is stored per
 * connection, computing the URL based on connection count may not give the desired result when using a connection pool.
 * Set {@link #useConnectionCount} to false if using this strategy with a connection pool.
 *
 * @author  Middleware Services
 */
public class RoundRobinConnectionStrategy implements ConnectionStrategy
{

  /** Internal method invocation counter. */
  private int invocationCount;

  /**
   * Whether {@link #getLdapUrls(ConnectionFactoryMetadata)} should use the connectionCount parameter or the {@link
   * #invocationCount}.
   */
  private final boolean useConnectionCount;


  /** Creates a new round robin connection strategy. */
  public RoundRobinConnectionStrategy()
  {
    this(true);
  }


  /**
   * Creates a new round robin connection strategy.
   *
   * @param  b  whether {@link #getLdapUrls(ConnectionFactoryMetadata)} should use the connectionCount parameter
   */
  public RoundRobinConnectionStrategy(final boolean b)
  {
    useConnectionCount = b;
  }


  /**
   * Return a list of URLs that cycles the list order. The first entry is moved to the end of the list for each
   * invocation.
   *
   * @param  metadata  which can be used to produce the URL list
   *
   * @return  list of URLs to attempt connections to
   */
  @Override
  public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
  {
    if (metadata == null || metadata.getLdapUrl() == null) {
      return null;
    }

    final List<String> l = new ArrayList<>(Arrays.asList(metadata.getLdapUrl().split(" ")));
    final int count = getCount(metadata.getConnectionCount());
    for (int i = 0; i < count % l.size(); i++) {
      l.add(l.remove(0));
    }
    return l.toArray(new String[l.size()]);
  }


  /**
   * Returns the supplied connection count if {@link #useConnectionCount} is true. Otherwise returns {@link
   * #invocationCount}.
   *
   * @param  connectionCount  as reported by the connection
   *
   * @return  count used to reorder the URL list
   */
  protected int getCount(final int connectionCount)
  {
    if (useConnectionCount) {
      return connectionCount;
    }
    return returnAndIncrementInvocationCount();
  }


  /**
   * Increments the internal invocation count and returns the previous value.
   *
   * @return  previous invocation count
   */
  private int returnAndIncrementInvocationCount()
  {
    final int i = invocationCount;
    invocationCount++;
    // reset the count if it exceeds the size of an integer
    if (invocationCount < 0) {
      invocationCount = 0;
    }
    return i;
  }
}
