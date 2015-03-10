/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

/**
 * Search results listener.
 *
 * @author  Middleware Services
 */
public interface SearchListener extends ResponseListener
{


  /**
   * Invoked when a search item is received from a provider.
   *
   * @param  item  containing a search result entry, reference, or intermediate response
   */
  void searchItemReceived(SearchItem item);
}
