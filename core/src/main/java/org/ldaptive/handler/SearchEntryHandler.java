/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;

/**
 * Provides post search handling of a search entry.
 *
 * @author  Middleware Services
 */
public interface SearchEntryHandler extends Handler<SearchRequest, SearchEntry>
{


  @Override
  HandlerResult<SearchEntry> handle(
    Connection conn,
    SearchRequest request,
    SearchEntry entry)
    throws LdapException;


  /**
   * Initialize the search request for use with this entry handler.
   *
   * @param  request  to initialize for this entry handler
   */
  void initializeRequest(SearchRequest request);
}
