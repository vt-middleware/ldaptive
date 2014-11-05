/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class that contains common implementations of {@link
 * ConnectionStrategy}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3006 $ $Date: 2014-07-02 10:22:50 -0400 (Wed, 02 Jul 2014) $
 */
public final class ConnectionStrategies
{


  /** Default constructor. */
  private ConnectionStrategies() {}


  /**
   * Takes a space delimited string of URLs and returns a list of URLs.
   *
   * @param  url  to split
   *
   * @return  list of URLs
   */
  protected static List<String> splitLdapUrl(final String url)
  {
    final List<String> urls = new ArrayList<>();
    if (url != null) {
      final StringTokenizer st = new StringTokenizer(url);
      while (st.hasMoreTokens()) {
        urls.add(st.nextToken());
      }
    } else {
      urls.add(null);
    }
    return urls;
  }


  /** Default strategy. */
  public static class DefaultConnectionStrategy implements ConnectionStrategy
  {


    /**
     * Returns an array containing a single entry URL that is the supplied url.
     *
     * @param  metadata  which can be used to produce the URL list
     *
     * @return  list of URLs to attempt connections to
     */
    @Override
    public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
    {
      return new String[] {metadata.getLdapUrl()};
    }
  }


  /** Active-Passive strategy. */
  public static class ActivePassiveConnectionStrategy
    implements ConnectionStrategy
  {


    /**
     * Return the URLs in the order they are provided, so that the first URL is
     * always tried first, then the second, and so forth.
     *
     * @param  metadata  which can be used to produce the URL list
     *
     * @return  list of URLs to attempt connections to
     */
    @Override
    public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
    {
      final List<String> l = splitLdapUrl(metadata.getLdapUrl());
      return l.toArray(new String[l.size()]);
    }
  }


  /** Round Robin strategy. */
  public static class RoundRobinConnectionStrategy implements ConnectionStrategy
  {

    /** Internal method invocation counter. */
    private int invocationCount;

    /**
     * Whether {@link #getLdapUrls(ConnectionFactoryMetadata)} should used the
     * connectionCount parameter or the {@link #invocationCount}.
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
     * @param  b  whether {@link #getLdapUrls(ConnectionFactoryMetadata)} should
     * used the connectionCount parameter
     */
    public RoundRobinConnectionStrategy(final boolean b)
    {
      useConnectionCount = b;
    }


    /**
     * Return a list of URLs that cycles the list order. The first entry is
     * moved to the end of the list for each invocation.
     *
     * @param  metadata  which can be used to produce the URL list
     *
     * @return  list of URLs to attempt connections to
     */
    @Override
    public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
    {
      final List<String> l = splitLdapUrl(metadata.getLdapUrl());
      final int count = getCount(metadata.getConnectionCount());
      for (int i = 0; i < count % l.size(); i++) {
        l.add(l.remove(0));
      }
      return l.toArray(new String[l.size()]);
    }


    /**
     * Returns the supplied connection count if {@link #useConnectionCount} is
     * true. Otherwise returns {@link #invocationCount}.
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


  /** Random strategy. */
  public static class RandomConnectionStrategy implements ConnectionStrategy
  {


    /**
     * Return a list of URLs in random order.
     *
     * @param  metadata  which can be used to produce the URL list
     *
     * @return  list of URLs to attempt connections to
     */
    @Override
    public String[] getLdapUrls(final ConnectionFactoryMetadata metadata)
    {
      final List<String> l = splitLdapUrl(metadata.getLdapUrl());
      Collections.shuffle(l);
      return l.toArray(new String[l.size()]);
    }
  }
}
