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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface SearchReferenceHandler
  extends Handler<SearchRequest, SearchReference>
{


  /** {@inheritDoc} */
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
