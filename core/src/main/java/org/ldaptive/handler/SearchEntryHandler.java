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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface SearchEntryHandler extends Handler<SearchRequest, SearchEntry>
{


  /** {@inheritDoc} */
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
