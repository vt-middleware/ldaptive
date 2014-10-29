/*
  $Id: AbstractSearchExecutor.java 2885 2014-02-05 21:28:49Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2885 $
  Updated: $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
*/
package org.ldaptive.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.ldaptive.Connection;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.cache.Cache;
import org.ldaptive.handler.OperationExceptionHandler;
import org.ldaptive.handler.OperationResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for concurrent search executors.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractSearchExecutor extends SearchRequest
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** To submit operations to. */
  private final ExecutorService service;

  /** Handler to handle search exceptions. */
  private OperationExceptionHandler<SearchRequest, SearchResult>
  searchExceptionHandler;

  /** Handlers to handle search responses. */
  private OperationResponseHandler<SearchRequest, SearchResult>[]
  searchResponseHandlers;

  /** Cache to use when performing searches. */
  private Cache<SearchRequest> searchCache;


  /**
   * Creates a new abstract search executor.
   *
   * @param  es  executor service
   */
  public AbstractSearchExecutor(final ExecutorService es)
  {
    if (es == null) {
      throw new NullPointerException("ExecutorService cannot be null");
    }
    service = es;
  }


  /**
   * Returns the executor service for this search executor.
   *
   * @return  executor service
   */
  protected ExecutorService getExecutorService()
  {
    return service;
  }


  /**
   * Returns the search exception handler.
   *
   * @return  search exception handler
   */
  public OperationExceptionHandler<SearchRequest, SearchResult>
  getSearchExceptionHandler()
  {
    return searchExceptionHandler;
  }


  /**
   * Sets the search exception handler.
   *
   * @param  handler  search exception handler
   */
  public void setSearchExceptionHandler(
    final OperationExceptionHandler<SearchRequest, SearchResult> handler)
  {
    searchExceptionHandler = handler;
  }


  /**
   * Returns the search response handlers.
   *
   * @return  search response handlers
   */
  public OperationResponseHandler<SearchRequest, SearchResult>[]
  getSearchResponseHandlers()
  {
    return searchResponseHandlers;
  }


  /**
   * Sets the search response handlers.
   *
   * @param  handlers  search response handlers
   */
  public void setSearchResponseHandlers(
    final OperationResponseHandler<SearchRequest, SearchResult>... handlers)
  {
    searchResponseHandlers = handlers;
  }


  /**
   * Returns the search cache.
   *
   * @return  cache
   */
  public Cache<SearchRequest> getSearchCache()
  {
    return searchCache;
  }


  /**
   * Sets the search cache.
   *
   * @param  cache  to set
   */
  public void setSearchCache(final Cache<SearchRequest> cache)
  {
    searchCache = cache;
  }


  /**
   * Shuts down the executor service. See {@link ExecutorService#shutdown()}.
   */
  public void shutdown()
  {
    service.shutdown();
  }


  /**
   * Immediately shuts down the executor service. See {@link
   * ExecutorService#shutdownNow()}.
   *
   * @return  list of tasks that never executed
   */
  public List<Runnable> shutdownNow()
  {
    return service.shutdownNow();
  }


  /**
   * Creates a new search operation configured with the properties on this
   * search executor.
   *
   * @param  conn  to pass to the search operation
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation(final Connection conn)
  {
    final SearchOperation op = new SearchOperation(conn);
    if (searchExceptionHandler != null) {
      op.setOperationExceptionHandler(searchExceptionHandler);
    }
    if (searchResponseHandlers != null) {
      op.setOperationResponseHandlers(searchResponseHandlers);
    }
    if (searchCache != null) {
      op.setCache(searchCache);
    }
    return op;
  }


  /** {@inheritDoc} */
  @Override
  protected void finalize()
    throws Throwable
  {
    try {
      shutdown();
    } finally {
      super.finalize();
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::service=%s, searchExceptionHandler=%s, " +
        "searchResponseHandlers=%s, searchCache=%s]",
        getClass().getName(),
        hashCode(),
        service,
        searchExceptionHandler,
        Arrays.toString(searchResponseHandlers),
        searchCache);
  }
}
