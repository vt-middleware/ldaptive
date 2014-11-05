/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

/**
 * Search results listener.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface SearchListener extends ResponseListener
{


  /**
   * Invoked when a search item is received from a provider.
   *
   * @param  item  containing a search result entry, reference, or intermediate
   * response
   */
  void searchItemReceived(SearchItem item);
}
