/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.control.PagedResultsControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the paged results control.
 *
 * @author  Middleware Services
 */
public class PagedResultsClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to invoke the search operation on. */
  private final Connection connection;

  /** Results page size. */
  private final int resultSize;


  /**
   * Creates a new paged results client.
   *
   * @param  conn  to execute the search operation on
   * @param  size  the results page size to request
   */
  public PagedResultsClient(final Connection conn, final int size)
  {
    connection = conn;
    resultSize = size;
  }


  /**
   * Performs a search operation with the {@link PagedResultsControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PagedResultsControl}</li>
   * </ul>
   *
   * @param  request  search request to execute
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> execute(final SearchRequest request)
    throws LdapException
  {
    return execute(request, new DefaultCookieManager());
  }


  /**
   * Performs a search operation with the {@link PagedResultsControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PagedResultsControl}</li>
   * </ul>
   *
   * <p>The cookie is extracted from the supplied response and replayed in the request.</p>
   *
   * @param  request  search request to execute
   * @param  response  of a previous paged results operation
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> execute(final SearchRequest request, final Response<SearchResult> response)
    throws LdapException
  {
    final byte[] cookie = getPagedResultsCookie(response);
    if (cookie == null) {
      throw new IllegalArgumentException("Response does not contain a paged results cookie");
    }

    return execute(request, new DefaultCookieManager(cookie));
  }


  /**
   * Performs a search operation with the {@link PagedResultsControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PagedResultsControl}</li>
   * </ul>
   *
   * <p>The cookie used in the request is read from the cookie manager and written to the cookie manager after a
   * successful search, if the response contains a cookie.</p>
   *
   * @param  request  search request to execute
   * @param  manager  for reading and writing cookies
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> execute(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    final SearchOperation search = new SearchOperation(connection);
    request.setControls(new PagedResultsControl(resultSize, manager.readCookie(), true));

    final Response<SearchResult> response = search.execute(request);
    final byte[] cookie = getPagedResultsCookie(response);
    if (cookie != null) {
      manager.writeCookie(cookie);
    }
    return response;
  }


  /**
   * Returns whether {@link #execute(SearchRequest, Response)} can be invoked again.
   *
   * @param  response  of a previous paged results operation
   *
   * @return  whether more paged search results can be retrieved from the server
   */
  public boolean hasMore(final Response<SearchResult> response)
  {
    return getPagedResultsCookie(response) != null;
  }


  /**
   * Performs a search operation with the {@link PagedResultsControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PagedResultsControl}</li>
   * </ul>
   *
   * <p>This method will continue to execute search operations until all paged search results have been retrieved from
   * the server. The returned response contains the response data of the last paged result operation plus the entries
   * and references returned by all previous search operations.</p>
   *
   * @param  request  search request to execute
   *
   * @return  search operation response of the last paged result operation
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> executeToCompletion(final SearchRequest request)
    throws LdapException
  {
    return executeToCompletion(request, new DefaultCookieManager());
  }


  /**
   * Performs a search operation with the {@link PagedResultsControl}. The supplied request is modified in the following
   * way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     PagedResultsControl}</li>
   * </ul>
   *
   * <p>This method will continue to execute search operations until all paged search results have been retrieved from
   * the server. The returned response contains the response data of the last paged result operation plus the entries
   * and references returned by all previous search operations.</p>
   *
   * <p>The cookie used for each request is read from the cookie manager and written to the cookie manager after a
   * successful search, if the response contains a cookie.</p>
   *
   * @param  request  search request to execute
   * @param  manager  for reading and writing cookies
   *
   * @return  search operation response of the last paged result operation
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> executeToCompletion(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    Response<SearchResult> response = null;
    final SearchResult result = new SearchResult();
    final SearchOperation search = new SearchOperation(connection);
    byte[] cookie = manager.readCookie();
    do {
      if (response != null && response.getResult() != null) {
        result.addEntries(response.getResult().getEntries());
        result.addReferences(response.getResult().getReferences());
      }
      request.setControls(new PagedResultsControl(resultSize, cookie, true));
      response = search.execute(request);
      cookie = getPagedResultsCookie(response);
      if (cookie != null) {
        manager.writeCookie(cookie);
      }
    } while (cookie != null);
    response.getResult().addEntries(result.getEntries());
    response.getResult().addReferences(result.getReferences());
    return response;
  }


  /**
   * Returns the paged results cookie in the supplied response or null if no cookie exists.
   *
   * @param  response  of a previous paged results operation
   *
   * @return  paged results cookie or null
   */
  protected byte[] getPagedResultsCookie(final Response<SearchResult> response)
  {
    byte[] cookie = null;
    final PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
    if (ctl != null) {
      if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
        cookie = ctl.getCookie();
      }
    }
    return cookie;
  }
}
