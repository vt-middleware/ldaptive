/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.SearchEntryHandler;

/**
 * Executes a list of search filters in parallel over a list of connection factories. This implementation executes each
 * search on the same connection in separate threads. If you need parallel searches over a pool of connections see
 * {@link AggregatePooledSearchExecutor}. A cached thread pool is used by default.
 *
 * @author  Middleware Services
 */
public class AggregateSearchExecutor extends AbstractAggregateSearchExecutor<ConnectionFactory>
{


  /** Default constructor. */
  public AggregateSearchExecutor()
  {
    this(Executors.newCachedThreadPool());
  }


  /**
   * Creates a new aggregate search executor.
   *
   * @param  es  executor service
   */
  public AggregateSearchExecutor(final ExecutorService es)
  {
    super(es);
  }


  @Override
  public Collection<Response<SearchResult>> search(
    final ConnectionFactory[] factories,
    final SearchFilter[] filters,
    final String[] attrs,
    final SearchEntryHandler... handlers)
    throws LdapException
  {
    final CompletionService<Collection<Response<SearchResult>>> searches = new ExecutorCompletionService<>(
      getExecutorService());
    final SearchRequest[] requests = new SearchRequest[filters.length];
    for (int i = 0; i < filters.length; i++) {
      final SearchRequest sr = newSearchRequest(this);
      if (filters[i] != null) {
        sr.setSearchFilter(filters[i]);
      }
      if (attrs != null) {
        sr.setReturnAttributes(attrs);
      }
      if (handlers != null) {
        sr.setSearchEntryHandlers(handlers);
      }
      requests[i] = sr;
    }

    final List<Future<Collection<Response<SearchResult>>>> futures = new ArrayList<>(factories.length * filters.length);
    for (ConnectionFactory factory : factories) {
      final Connection conn = factory.getConnection();
      final SearchOperation op = createSearchOperation(conn);
      final SearchOperationWorker worker = new SearchOperationWorker(op, getExecutorService());
      futures.add(searches.submit(createCallable(conn, worker, requests)));
    }

    final List<Response<SearchResult>> responses = new ArrayList<>(factories.length * filters.length);
    for (Future<Collection<Response<SearchResult>>> future : futures) {
      try {
        responses.addAll(future.get());
      } catch (ExecutionException e) {
        logger.debug("ExecutionException thrown, ignoring", e);
      } catch (InterruptedException e) {
        logger.warn("InterruptedException thrown, ignoring", e);
      }
    }
    return responses;
  }


  /**
   * Returns a {@link Callable} that executes the supplied request with the supplied worker in a try-finally block that
   * opens and closes the connection.
   *
   * @param  <Q>  type of ldap request
   * @param  <S>  type of ldap response
   * @param  conn  connection that the worker will execute on
   * @param  worker  to execute
   * @param  requests  to pass to the worker
   *
   * @return  callable for the supplied operation and request
   */
  protected static <Q extends Request, S> Callable<Collection<Response<S>>> createCallable(
    final Connection conn,
    final OperationWorker<Q, S> worker,
    final Q[] requests)
  {
    return
      new Callable<Collection<Response<S>>>() {
      @Override
      public Collection<Response<S>> call()
        throws LdapException
      {
        try {
          conn.open();
          return worker.executeToCompletion(requests);
        } finally {
          conn.close();
        }
      }
    };
  }
}
