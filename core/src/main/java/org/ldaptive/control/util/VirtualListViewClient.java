/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.AbstractSearchOperationFactory;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
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
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     SortRequestControl} and {@link org.ldaptive.control.VirtualListViewRequestControl}</li>
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
    request.setControls(sortControl, params.createRequestControl(true));
    return search.execute(request);
  }


  /**
   * Performs a search operation with the {@link org.ldaptive.control.VirtualListViewRequestControl}. The supplied
   * request is modified in the following way:
   *
   * <ul>
   *   <li>{@link SearchRequest#setControls( org.ldaptive.control.RequestControl...)} is invoked with {@link
   *     SortRequestControl} and {@link org.ldaptive.control.VirtualListViewRequestControl}</li>
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
    request.setControls(sortControl, params.createRequestControl(result, true));
    return search.execute(request);
  }
}
