/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;

/**
 * Executes an ldap search operation on a separate thread.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SearchOperationWorker
  extends AbstractOperationWorker<SearchRequest, SearchResult>
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
  public SearchOperationWorker(
    final SearchOperation op,
    final ExecutorService es)
  {
    super(op, es);
  }
}
