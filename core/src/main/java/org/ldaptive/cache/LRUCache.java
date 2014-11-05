/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;

/**
 * Least-Recently-Used cache implementation. Leverages a {@link LinkedHashMap}.
 *
 * @param  <Q>  type of search request
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class LRUCache<Q extends SearchRequest> implements Cache<Q>
{

  /** Initial capacity of the hash map. */
  private static final int INITIAL_CAPACITY = 16;

  /** Load factor of the hash map. */
  private static final float LOAD_FACTOR = 0.75f;

  /** Map to cache search results. */
  private Map<Q, Item> cache;

  /** Executor for performing eviction. */
  private final ScheduledExecutorService executor =
    Executors.newSingleThreadScheduledExecutor(
      new ThreadFactory() {
        @Override
        public Thread newThread(final Runnable r)
        {
          final Thread t = new Thread(r);
          t.setDaemon(true);
          return t;
        }
      });


  /**
   * Creates a new LRU cache.
   *
   * @param  size  number of results to cache
   * @param  timeToLive  in seconds that results should stay in the cache
   * @param  interval  in seconds to enforce timeToLive
   */
  public LRUCache(final int size, final long timeToLive, final long interval)
  {
    cache = new LinkedHashMap<Q, Item>(INITIAL_CAPACITY, LOAD_FACTOR, true) {

      /** serialVersionUID. */
      private static final long serialVersionUID = -4082551016104288539L;


      /** {@inheritDoc} */
      @Override
      protected boolean removeEldestEntry(final Map.Entry<Q, Item> entry)
      {
        return size() > size;
      }
    };

    final Runnable expire = new Runnable() {
      @Override
      public void run()
      {
        synchronized (cache) {
          final Iterator<Item> i = cache.values().iterator();
          final long t = System.currentTimeMillis();
          while (i.hasNext()) {
            final Item item = i.next();
            if (t - item.creationTime > TimeUnit.SECONDS.toMillis(timeToLive)) {
              i.remove();
            }
          }
        }
      }
    };
    executor.scheduleAtFixedRate(expire, interval, interval, TimeUnit.SECONDS);
  }


  /** Removes all data from this cache. */
  public void clear()
  {
    synchronized (cache) {
      cache.clear();
    }
  }


  /** {@inheritDoc} */
  @Override
  public SearchResult get(final Q request)
  {
    synchronized (cache) {
      if (cache.containsKey(request)) {
        return cache.get(request).result;
      } else {
        return null;
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public void put(final Q request, final SearchResult result)
  {
    synchronized (cache) {
      cache.put(request, new Item(result));
    }
  }


  /**
   * Returns the number of items in this cache.
   *
   * @return  size of this cache
   */
  public int size()
  {
    synchronized (cache) {
      return cache.size();
    }
  }


  /** Frees any resources associated with this cache. */
  public void close()
  {
    executor.shutdown();
    cache = null;
  }


  /** Container for data related to cached ldap results. */
  private class Item
  {

    /** Ldap result. */
    private final SearchResult result;

    /** Timestamp when this item is created. */
    private final long creationTime;


    /**
     * Creates a new item.
     *
     * @param  sr  search result
     */
    public Item(final SearchResult sr)
    {
      result = sr;
      creationTime = System.currentTimeMillis();
    }
  }
}
