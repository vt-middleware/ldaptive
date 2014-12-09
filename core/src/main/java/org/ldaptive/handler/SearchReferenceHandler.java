/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchReference;
import org.ldaptive.SearchRequest;

/**
 * Provides post search handling of a search reference.
 *
 * @author  Middleware Services
 */
public interface SearchReferenceHandler
  extends Handler<SearchRequest, SearchReference>
{


  @Override
  HandlerResult<SearchReference> handle(
    Connection conn,
    SearchRequest request,
    SearchReference reference)
    throws LdapException;


  /**
   * Initialize the search request for use with this reference handler.
   *
   * @param  request  to initialize for this reference handler
   */
  void initializeRequest(SearchRequest request);
}
