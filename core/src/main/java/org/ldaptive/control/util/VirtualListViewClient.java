/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that simplifies using the virtual list view control.
 *
 * @author  Middleware Services
 */
public class VirtualListViewClient
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to invoke the search operation on. */
  private final Connection connection;

  /** Used on the search operation. */
  private final SortRequestControl sortControl;


  /**
   * Creates a new virtual list view client.
   *
   * @param  conn  to execute the search operation on
   * @param  keys  to supply to a sort request control
   */
  public VirtualListViewClient(final Connection conn, final SortKey... keys)
  {
    connection = conn;
    sortControl = new SortRequestControl(keys);
  }


  /**
   * Performs a search operation with the {@link
   * org.ldaptive.control.VirtualListViewRequestControl}. The supplied request
   * is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls(
   *     org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     SortRequestControl} and {@link
   *     org.ldaptive.control.VirtualListViewRequestControl}</li>
   * </ul>
   *
   * @param  request  search request to execute
   * @param  params  virtual list view data
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> execute(
    final SearchRequest request,
    final VirtualListViewParams params)
    throws LdapException
  {
    final SearchOperation search = new SearchOperation(connection);
    request.setControls(sortControl, params.createRequestControl(true));
    return search.execute(request);
  }


  /**
   * Performs a search operation with the {@link
   * org.ldaptive.control.VirtualListViewRequestControl}. The supplied request
   * is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls(
   *     org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     SortRequestControl} and {@link
   *     org.ldaptive.control.VirtualListViewRequestControl}</li>
   * </ul>
   *
   * <p>The content count and context id are extracted from the supplied
   * response and replayed as appropriate in the request.</p>
   *
   * @param  request  search request to execute
   * @param  params  virtual list view data
   * @param  response  of a previous VLV operation
   *
   * @return  search operation response
   *
   * @throws  LdapException  if the search fails
   */
  public Response<SearchResult> execute(
    final SearchRequest request,
    final VirtualListViewParams params,
    final Response<SearchResult> response)
    throws LdapException
  {
    final SearchOperation search = new SearchOperation(connection);
    request.setControls(
      sortControl,
      params.createRequestControl(response, true));
    return search.execute(request);
  }
}
