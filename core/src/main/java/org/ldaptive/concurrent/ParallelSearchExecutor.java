/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.SearchEntryHandler;

/**
 * Executes a list of search filters in parallel. This implementation executes
 * each search on the same connection in separate threads. If you need parallel
 * searches over a pool of connections see {@link ParallelPooledSearchExecutor}.
 * A cached thread pool is used by default.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ParallelSearchExecutor
  extends AbstractParallelSearchExecutor<ConnectionFactory>
{

  /** Default constructor. */
  public ParallelSearchExecutor()
  {
    this(Executors.newCachedThreadPool());
  }


  /**
   * Creates a new parallel search executor.
   *
   * @param  es  executor service
   */
  public ParallelSearchExecutor(final ExecutorService es)
  {
    super(es);
  }


  /** {@inheritDoc} */
  @Override
  public Collection<Response<SearchResult>> search(
    final ConnectionFactory factory,
    final SearchFilter[] filters,
    final String[] attrs,
    final SearchEntryHandler... handlers)
    throws LdapException
  {
    Collection<Response<SearchResult>> response = null;
    final Connection conn = factory.getConnection();
    try {
      conn.open();

      final SearchOperation op = createSearchOperation(conn);

      final SearchOperationWorker worker = new SearchOperationWorker(
        op,
        getExecutorService());
      final SearchRequest[] sr = new SearchRequest[filters.length];
      for (int i = 0; i < filters.length; i++) {
        sr[i] = newSearchRequest(this);
        if (filters[i] != null) {
          sr[i].setSearchFilter(filters[i]);
        }
        if (attrs != null) {
          sr[i].setReturnAttributes(attrs);
        }
        if (handlers != null) {
          sr[i].setSearchEntryHandlers(handlers);
        }
      }
      response = worker.executeToCompletion(sr);
    } finally {
      conn.close();
    }
    return response;
  }
}
