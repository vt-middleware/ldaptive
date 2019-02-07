/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.handler.LdapEntryHandler;

/**
 * Executes a list of search filters in parallel over a list of connection factories. A cached thread pool is used by
 * default.
 *
 * @author  Middleware Services
 */
public class AggregateSearchOperation extends AbstractConcurrentSearchOperation
{


  /** Default constructor. */
  public AggregateSearchOperation()
  {
    this(Executors.newCachedThreadPool());
  }


  /**
   * Creates a new aggregate search executor.
   *
   * @param  es  executor service
   */
  public AggregateSearchOperation(final ExecutorService es)
  {
    super(es);
  }


  /**
   * Performs a search operation with the supplied connection factories.
   *
   * @param  factories  to get a connection from
   * @param  filters  to search with
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(final ConnectionFactory[] factories, final String... filters)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return execute(factories, sf, null, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factories.
   *
   * @param  factories  to get a connection from
   * @param  filters  to search with
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(final ConnectionFactory[] factories, final SearchFilter[] filters)
    throws LdapException
  {
    return execute(factories, filters, null, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factories.
   *
   * @param  factories  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory[] factories,
    final String[] filters,
    final String... attrs)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return execute(factories, sf, attrs, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factories.
   *
   * @param  factories  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory[] factories,
    final SearchFilter[] filters,
    final String... attrs)
    throws LdapException
  {
    return execute(factories, filters, attrs, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factories.
   *
   * @param  factories  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   * @param  handlers  entry handlers
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory[] factories,
    final SearchFilter[] filters,
    final String[] attrs,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    final int totalSize = factories.length * filters.length;
    final CompletionService<Collection<SearchResponse>> searches = new ExecutorCompletionService<>(
      getExecutorService(),
      new ArrayBlockingQueue<>(totalSize));
    final SearchRequest[] requests = createSearchRequests(filters, attrs);

    final List<Future<Collection<SearchResponse>>> futures = new ArrayList<>(totalSize);
    for (ConnectionFactory factory : factories) {
      final SearchOperation op = createSearchOperation(factory, handlers);
      final SearchOperationWorker worker = new SearchOperationWorker(op, getExecutorService());
      futures.add(searches.submit(() -> worker.executeToCompletion(requests)));
    }

    final List<SearchResponse> responses = new ArrayList<>(totalSize);
    for (Future<Collection<SearchResponse>> future : futures) {
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
}
