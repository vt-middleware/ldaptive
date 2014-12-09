/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.cache.Cache;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.intermediate.IntermediateResponse;
import org.ldaptive.provider.SearchItem;
import org.ldaptive.provider.SearchIterator;

/**
 * Executes an ldap search operation.
 *
 * @author  Middleware Services
 */
public class SearchOperation
  extends AbstractOperation<SearchRequest, SearchResult>
{

  /** Cache to use when performing searches. */
  private Cache<SearchRequest> cache;


  /**
   * Creates a new search operation.
   *
   * @param  conn  connection
   */
  public SearchOperation(final Connection conn)
  {
    super(conn);
  }


  /**
   * Creates a new search operation.
   *
   * @param  conn  connection
   * @param  c  cache
   */
  public SearchOperation(final Connection conn, final Cache<SearchRequest> c)
  {
    super(conn);
    cache = c;
  }


  /**
   * Returns the cache to check when performing search operations. When a cache
   * is provided it will be populated as new searches are performed and used
   * when a search request hits the cache.
   *
   * @return  cache
   */
  public Cache<SearchRequest> getCache()
  {
    return cache;
  }


  /**
   * Sets the cache.
   *
   * @param  c  cache to set
   */
  public void setCache(final Cache<SearchRequest> c)
  {
    cache = c;
  }


  @Override
  protected Response<SearchResult> invoke(final SearchRequest request)
    throws LdapException
  {
    Response<SearchResult> response;
    if (cache != null) {
      final SearchResult sr = cache.get(request);
      if (sr == null) {
        response = executeSearch(request);
        cache.put(request, response.getResult());
        logger.debug("invoke stored result={} in cache", response.getResult());
      } else {
        logger.debug("invoke found result={} in cache", sr);
        response = new Response<>(sr, null);
      }
    } else {
      response = executeSearch(request);
    }
    return response;
  }


  /**
   * Performs the ldap search.
   *
   * @param  request  to invoke search with
   *
   * @return  ldap response
   *
   * @throws  LdapException  if an error occurs
   */
  protected Response<SearchResult> executeSearch(final SearchRequest request)
    throws LdapException
  {
    final SearchIterator si = getConnection().getProviderConnection().search(
      request);
    final SearchResult result = readResult(request, si);
    final Response<Void> response = si.getResponse();
    return
      new Response<>(
        result,
        response.getResultCode(),
        response.getMessage(),
        response.getMatchedDn(),
        response.getControls(),
        response.getReferralURLs(),
        response.getMessageId());
  }


  /**
   * Invokes the provider search operation and iterates over the results.
   * Invokes handlers as necessary for each result type.
   *
   * @param  request  used to create the search iterator
   * @param  si  search iterator
   *
   * @return  search result
   *
   * @throws  LdapException  if an error occurs
   */
  protected SearchResult readResult(
    final SearchRequest request,
    final SearchIterator si)
    throws LdapException
  {
    final SearchResult result = new SearchResult(request.getSortBehavior());
    try {
      while (si.hasNext()) {
        final SearchItem item = si.next();
        logger.trace("Received search item={}", item);
        if (item.isSearchEntry()) {
          final SearchEntry se = item.getSearchEntry();
          if (se != null) {
            final HandlerResult<SearchEntry> hr = executeHandlers(
              request.getSearchEntryHandlers(),
              request,
              se);
            if (hr.getResult() != null) {
              result.addEntry(hr.getResult());
            }
            if (hr.getAbort()) {
              logger.debug("Aborting search on entry=%s", se);
              break;
            }
          }
        } else if (item.isSearchReference()) {
          final SearchReference sr = item.getSearchReference();
          if (sr != null) {
            final HandlerResult<SearchReference> hr = executeHandlers(
              request.getSearchReferenceHandlers(),
              request,
              sr);
            if (hr.getResult() != null) {
              result.addReference(hr.getResult());
            }
            if (hr.getAbort()) {
              logger.debug("Aborting search on reference=%s", sr);
              break;
            }
          }
        } else if (item.isIntermediateResponse()) {
          final IntermediateResponse ir = item.getIntermediateResponse();
          if (ir != null) {
            final HandlerResult<IntermediateResponse> hr = executeHandlers(
              request.getIntermediateResponseHandlers(),
              request,
              ir);
            if (hr.getAbort()) {
              logger.debug("Aborting search on intermediate response=%s", ir);
              break;
            }
          }
        }
      }
    } finally {
      si.close();
    }
    return result;
  }
}
