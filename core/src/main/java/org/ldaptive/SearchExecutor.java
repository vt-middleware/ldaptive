/*
  $Id: SearchExecutor.java 2885 2014-02-05 21:28:49Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2885 $
  Updated: $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
*/
package org.ldaptive;

import org.ldaptive.cache.Cache;
import org.ldaptive.handler.OperationExceptionHandler;
import org.ldaptive.handler.OperationResponseHandler;
import org.ldaptive.handler.SearchEntryHandler;

/**
 * Helper class which encapsulates the try, finally idiom used to execute a
 * {@link SearchOperation}. This is a convenience class for searching if you
 * don't need to manage individual connections. In addition, this class provides
 * a way to hold common search request properties constant while changing
 * properties that tend to be more dynamic.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SearchExecutor extends SearchRequest
{

  /** Handler to handle search exceptions. */
  private OperationExceptionHandler<SearchRequest, SearchResult>
  searchExceptionHandler;

  /** Handlers to handle search responses. */
  private OperationResponseHandler<SearchRequest, SearchResult>[]
  searchResponseHandlers;

  /** Cache to use when performing searches. */
  private Cache<SearchRequest> searchCache;


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
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(final ConnectionFactory factory)
    throws LdapException
  {
    return search(factory, null, (String[]) null, (SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(
    final ConnectionFactory factory,
    final String filter)
    throws LdapException
  {
    return
      search(
        factory,
        new SearchFilter(filter),
        (String[]) null,
        (SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(
    final ConnectionFactory factory,
    final SearchFilter filter)
    throws LdapException
  {
    return
      search(factory, filter, (String[]) null, (SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(
    final ConnectionFactory factory,
    final String filter,
    final String... attrs)
    throws LdapException
  {
    return
      search(
        factory,
        new SearchFilter(filter),
        attrs,
        (SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(
    final ConnectionFactory factory,
    final SearchFilter filter,
    final String... attrs)
    throws LdapException
  {
    return search(factory, filter, attrs, (SearchEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filter  to search with
   * @param  attrs  to return
   * @param  handlers  entry handlers
   *
   * @return  search result
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> search(
    final ConnectionFactory factory,
    final SearchFilter filter,
    final String[] attrs,
    final SearchEntryHandler... handlers)
    throws LdapException
  {
    Response<SearchResult> response = null;
    final Connection conn = factory.getConnection();
    try {
      conn.open();

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

      final SearchRequest sr = newSearchRequest(this);
      if (filter != null) {
        sr.setSearchFilter(filter);
      }
      if (attrs != null) {
        sr.setReturnAttributes(attrs);
      }
      if (handlers != null) {
        sr.setSearchEntryHandlers(handlers);
      }
      response = op.execute(sr);
    } finally {
      conn.close();
    }
    return response;
  }
}
