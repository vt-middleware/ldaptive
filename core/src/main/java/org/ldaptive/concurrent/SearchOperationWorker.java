/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;

/**
 * Executes an ldap search operation on a separate thread.
 *
 * @author  Middleware Services
 */
public class SearchOperationWorker extends AbstractOperationWorker<SearchRequest, SearchResponse>
{


  /**
   * Creates a new search operation worker.
   *
   * @param  op  search operation to execute
   */
  public SearchOperationWorker(final SearchOperation op)
  {
    super(op);
  }


  /**
   * Creates a new search operation worker.
   *
   * @param  op  search operation to execute
   * @param  es  executor service
   */
  public SearchOperationWorker(final SearchOperation op, final ExecutorService es)
  {
    super(op, es);
  }
}
