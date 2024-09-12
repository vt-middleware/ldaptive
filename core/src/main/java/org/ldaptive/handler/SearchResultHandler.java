/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.function.Function;
import org.ldaptive.SearchResponse;

/**
 * Marker interface for a search result handler.
 *
 * @author  Middleware Services
 */
public interface SearchResultHandler extends Function<SearchResponse, SearchResponse>
{


  /** Intended usage of the handler. */
  enum Usage {
    /** Use this handler with {@link org.ldaptive.SearchOperation#send()}*/
    ASYNC,

    /** Use this handler with {@link org.ldaptive.SearchOperation#execute()}*/
    SYNC
  }


  /**
   * Returns the usage for this handler.
   *
   * @return  handler usage
   */
  default Usage getUsage()
  {
    return Usage.SYNC;
  }
}
