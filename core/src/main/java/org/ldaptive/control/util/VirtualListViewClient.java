/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.control.VirtualListViewRequestControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.ldaptive.handler.LdapEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the virtual list view control.
 *
 * @author  Middleware Services
 */
public class VirtualListViewClient extends AbstractSearchOperationFactory
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Used on the search operation. */
  private final SortRequestControl sortControl;


  /**
   * Creates a new virtual list view client.
   *
   * @param  cf  to get a connection from
   * @param  keys  to supply to a sort request control
   */
  public VirtualListViewClient(final ConnectionFactory cf, final SortKey... keys)
  {
    setConnectionFactory(cf);
    sortControl = new SortRequestControl(keys);
  }


  /**
   * Performs a search operation with the {@link org.ldaptive.control.VirtualListViewRequestControl}. The supplied
   * request is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( RequestControl...)} is invoked with {@link SortRequestControl} and {@link
   *   VirtualListViewRequestControl} and any other controls previously set on the search request.</li>
   * </ul>
   *
   * @param  request  search request to execute
   * @param  params  virtual list view data
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse execute(final SearchRequest request, final VirtualListViewParams params)
    throws LdapException
  {
    final SearchOperation search = createSearchOperation();
    request.setControls(appendRequestControls(request, params.createRequestControl(true)));
    final SearchResponse response = search.execute(request);
    final byte[] cookie = getVirtualListViewCookie(response);
    if (cookie != null && params.getCookieManager() != null) {
      params.getCookieManager().writeCookie(cookie);
    }
    return response;
  }


  /**
   * Performs a search operation with the {@link VirtualListViewRequestControl}. The supplied
   * request is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( RequestControl...)} is invoked with {@link SortRequestControl} and {@link
   *   VirtualListViewRequestControl} and any other controls previously set on the search request.</li>
   * </ul>
   *
   * <p>The content count and context id are extracted from the supplied response and replayed as appropriate in the
   * request.</p>
   *
   * @param  request  search request to execute
   * @param  params  virtual list view data
   * @param  result  of a previous VLV operation
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse execute(
    final SearchRequest request,
    final VirtualListViewParams params,
    final SearchResponse result)
    throws LdapException
  {
    final SearchOperation search = createSearchOperation();
    request.setControls(appendRequestControls(request, params.createRequestControl(result, true)));
    final SearchResponse response = search.execute(request);
    final byte[] cookie = getVirtualListViewCookie(response);
    if (cookie != null && params.getCookieManager() != null) {
      params.getCookieManager().writeCookie(cookie);
    }
    return response;
  }


  /**
   * Performs a search operation with the {@link VirtualListViewRequestControl}. The supplied request is modified in the
   * following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( RequestControl...)} is invoked with {@link SortRequestControl} and {@link
   *   VirtualListViewRequestControl} and any other controls previously set on the search request.</li>
   * </ul>
   *
   * <p>This method will continue to execute search operations until all search entries have been retrieved from the
   * server. The returned response contains the response data of the last search result operation plus the entries and
   * references returned by all previous search operations. The criteria used ot determine whether to continue searching
   * is that the last response contained a cookie, produced a success result code, has a greater than zero contentCount
   * and we have currently processed less entries than the contentCount.</p>
   *
   * <p>The cookie used for each request is read from the cookie manager and written to the cookie manager after a
   * successful search, if the response contains a cookie.</p>
   *
   * <p>This method builds a synthetic response which contains the results of all search operations. Any ordering
   * imposed by result handlers may be lost by this process.</p>
   *
   * @param  request  search request to execute
   * @param  params  virtual list view data
   *
   * @return  search operation response of the last paged result operation
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse executeToCompletion(
    final SearchRequest request,
    final VirtualListViewParams params)
    throws LdapException
  {
    SearchResponse result = null;
    final SearchResponse combinedResults = new SearchResponse();
    final SearchOperation search = createSearchOperation();
    final AtomicInteger entryCount = new AtomicInteger();
    final LdapEntryHandler[] handlers = search.getEntryHandlers();
    search.setEntryHandlers(LdapUtils.concatArrays(new LdapEntryHandler[] {e -> {
      entryCount.incrementAndGet();
      return e;
    }}, handlers));

    VirtualListViewParams newParams = params;
    byte[] cookie;
    int contentCount;
    ResultCode ctrlResult;
    do {
      if (result != null) {
        combinedResults.addEntries(result.getEntries());
        combinedResults.addReferences(result.getReferences());
        // move the target offset by the size of the after count
        newParams = new VirtualListViewParams(
          newParams.getTargetOffset() + newParams.getAfterCount() + 1, 0, params.getAfterCount());
        request.setControls(appendRequestControls(request, newParams.createRequestControl(result, true)));
      } else {
        request.setControls(appendRequestControls(request, newParams.createRequestControl(true)));
      }

      result = search.execute(request);
      final VirtualListViewResponseControl ctrl = getResponseControl(result);
      contentCount = ctrl != null ? ctrl.getContentCount() : 0;
      cookie = ctrl != null ? ctrl.getContextID() : null;
      ctrlResult = ctrl != null ? ctrl.getViewResult() : null;
      if (cookie != null) {
        newParams.getCookieManager().writeCookie(cookie);
      }
    } while (
      cookie != null && ResultCode.SUCCESS.equals(ctrlResult) && contentCount > 0 && entryCount.get() < contentCount);
    result.addEntries(combinedResults.getEntries());
    result.addReferences(combinedResults.getReferences());
    return result;
  }


  /**
   * Returns the {@link VirtualListViewResponseControl} in the supplied response.
   *
   * @param  result  to inspect for a response control
   *
   * @return  VLV response control or null if it does not exist
   */
  public VirtualListViewResponseControl getResponseControl(final SearchResponse result)
  {
    return (VirtualListViewResponseControl) result.getControl(VirtualListViewResponseControl.OID);
  }


  /**
   * Returns the VLV results cookie in the supplied response or null if no cookie exists.
   *
   * @param  result  of a previous VLV results operation
   *
   * @return  VLV results cookie or null
   */
  protected byte[] getVirtualListViewCookie(final SearchResponse result)
  {
    byte[] cookie = null;
    final VirtualListViewResponseControl ctl = (VirtualListViewResponseControl) result.getControl(
      VirtualListViewResponseControl.OID);
    if (ctl != null) {
      if (ctl.getContextID() != null && ctl.getContextID().length > 0) {
        cookie = ctl.getContextID();
      }
    }
    return cookie;
  }


  /**
   * Creates a new array of request controls which includes the VLV and sort controls. Any other request controls are
   * in included
   *
   * @param  request  to read controls from
   * @param  cntrl  VLV control to include
   *
   * @return  array of request controls ready to be used in a search operation
   */
  private RequestControl[] appendRequestControls(final SearchRequest request, final VirtualListViewRequestControl cntrl)
  {
    if (request.getControls() != null && request.getControls().length > 0) {
      final List<RequestControl> requestControls = Arrays.stream(
          request.getControls()).filter(c -> !(c instanceof VirtualListViewRequestControl) && !c.equals(sortControl))
        .collect(Collectors.toList());
      requestControls.add(sortControl);
      requestControls.add(cntrl);
      return requestControls.toArray(RequestControl[]::new);
    } else {
      return new RequestControl[] {sortControl, cntrl};
    }
  }
}
