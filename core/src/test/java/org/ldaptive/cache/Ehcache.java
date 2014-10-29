/*
  $Id: Ehcache.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.cache;

import net.sf.ehcache.Element;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;

/**
 * Ehcache implementation.
 *
 * @param  <Q>  type of search request
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class Ehcache<Q extends SearchRequest> implements Cache<Q>
{

  /** Underlying ehcache. */
  protected final net.sf.ehcache.Cache cache;


  /**
   * Creates a new ehcache.
   *
   * @param  c  backing ehcache
   */
  public Ehcache(final net.sf.ehcache.Cache c)
  {
    cache = c;
  }


  /** Removes all data from this cache. */
  public void clear()
  {
    cache.removeAll();
  }


  /** {@inheritDoc} */
  @Override
  public SearchResult get(final Q request)
  {
    final Element e = cache.get(request);
    if (e == null) {
      return null;
    }
    return (SearchResult) e.getObjectValue();
  }


  /** {@inheritDoc} */
  @Override
  public void put(final Q request, final SearchResult result)
  {
    cache.put(new Element(request, result));
  }


  /**
   * Returns the number of items in this cache.
   *
   * @return  size of this cache
   */
  public int size()
  {
    return cache.getKeysWithExpiryCheck().size();
  }
}
