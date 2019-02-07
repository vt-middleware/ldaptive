/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.handler.LdapEntryHandler;

/**
 * Executes a list of search filters in parallel. A cached thread pool is used by default.
 *
 * @author  Middleware Services
 */
public class ParallelSearchOperation extends AbstractConcurrentSearchOperation
{

  /** Default constructor. */
  public ParallelSearchOperation()
  {
    this(Executors.newCachedThreadPool());
  }


  /**
   * Creates a new parallel search executor.
   *
   * @param  es  executor service
   */
  public ParallelSearchOperation(final ExecutorService es)
  {
    super(es);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filters  to search with
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(final ConnectionFactory factory, final String... filters)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return execute(factory, sf, null, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filters  to search with
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(final ConnectionFactory factory, final SearchFilter... filters)
    throws LdapException
  {
    return execute(factory, filters, null, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory factory,
    final String[] filters,
    final String... attrs)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return execute(factory, sf, attrs, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory factory,
    final SearchFilter[] filters,
    final String... attrs)
    throws LdapException
  {
    return execute(factory, filters, attrs, (LdapEntryHandler[]) null);
  }


  /**
   * Performs a search operation with the supplied connection factory.
   *
   * @param  factory  to get a connection from
   * @param  filters  to search with
   * @param  attrs  to return
   * @param  handlers  entry handlers
   *
   * @return  search results
   *
   * @throws  LdapException  if the search fails
   */
  public Collection<SearchResponse> execute(
    final ConnectionFactory factory,
    final SearchFilter[] filters,
    final String[] attrs,
    final LdapEntryHandler... handlers)
    throws LdapException
  {
    final SearchOperation op = createSearchOperation(factory, handlers);
    final SearchOperationWorker worker = new SearchOperationWorker(op, getExecutorService());
    final SearchRequest[] sr = createSearchRequests(filters, attrs);
    return worker.executeToCompletion(sr);
  }
}
