/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.control.PagedResultsControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the paged results control.
 *
 * @author  Middleware Services
 */
public class PagedResultsClient extends AbstractSearchOperationFactory
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Results page size. */
  private final int resultSize;


  /**
   * Creates a new paged results client.
   *
   * @param  cf  to get a connection from
   * @param  size  the results page size to request
   */
  public PagedResultsClient(final ConnectionFactory cf, final int size)
  {
    setConnectionFactory(cf);
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
  public SearchResponse execute(final SearchRequest request)
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
   * @param  result  of a previous paged results operation
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse execute(final SearchRequest request, final SearchResponse result)
    throws LdapException
  {
    final byte[] cookie = getPagedResultsCookie(result);
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
  public SearchResponse execute(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    final SearchOperation search = createSearchOperation();
    request.setControls(new PagedResultsControl(resultSize, manager.readCookie(), true));
    final SearchResponse result = search.execute(request);
    final byte[] cookie = getPagedResultsCookie(result);
    if (cookie != null) {
      manager.writeCookie(cookie);
    }
    return result;
  }


  /**
   * Returns whether {@link #execute(SearchRequest, SearchResponse)} can be invoked again.
   *
   * @param  result  of a previous paged results operation
   *
   * @return  whether more paged search results can be retrieved from the server
   */
  public boolean hasMore(final SearchResponse result)
  {
    return getPagedResultsCookie(result) != null;
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
  public SearchResponse executeToCompletion(final SearchRequest request)
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
   * <p>This method builds a synthetic response which contains the results of all search operations. Any ordering
   * imposed by result handlers will be lost by this process.</p>
   *
   * @param  request  search request to execute
   * @param  manager  for reading and writing cookies
   *
   * @return  search operation response of the last paged result operation
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse executeToCompletion(final SearchRequest request, final CookieManager manager)
    throws LdapException
  {
    SearchResponse result = null;
    final SearchResponse combinedResults = new SearchResponse();
    final SearchOperation search = createSearchOperation();
    byte[] cookie = manager.readCookie();
    do {
      if (result != null) {
        combinedResults.addEntries(result.getEntries());
        combinedResults.addReferences(result.getReferences());
      }
      request.setControls(new PagedResultsControl(resultSize, cookie, true));
      result = search.execute(request);
      cookie = getPagedResultsCookie(result);
      if (cookie != null) {
        manager.writeCookie(cookie);
      }
    } while (cookie != null);
    result.addEntries(combinedResults.getEntries());
    result.addReferences(combinedResults.getReferences());
    return result;
  }


  /**
   * Returns the paged results cookie in the supplied response or null if no cookie exists.
   *
   * @param  result  of a previous paged results operation
   *
   * @return  paged results cookie or null
   */
  protected byte[] getPagedResultsCookie(final SearchResponse result)
  {
    byte[] cookie = null;
    final PagedResultsControl ctl = (PagedResultsControl) result.getControl(PagedResultsControl.OID);
    if (ctl != null) {
      if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
        cookie = ctl.getCookie();
      }
    }
    return cookie;
  }
}
