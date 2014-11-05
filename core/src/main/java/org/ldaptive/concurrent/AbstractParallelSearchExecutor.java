/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.SearchEntryHandler;

/**
 * Base class for parallel search executors.
 *
 * @param  <T>  type of connection factory
 *
 * @author  Middleware Services
 */
public abstract class
AbstractParallelSearchExecutor<T extends ConnectionFactory>
  extends AbstractSearchExecutor
{


  /**
   * Creates a new abstract parallel search executor.
   *
   * @param  es  executor service
   */
  public AbstractParallelSearchExecutor(final ExecutorService es)
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
  public Collection<Response<SearchResult>> search(
    final T factory,
    final String... filters)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return search(factory, sf, null, (SearchEntryHandler[]) null);
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
  public Collection<Response<SearchResult>> search(
    final T factory,
    final SearchFilter[] filters)
    throws LdapException
  {
    return
      search(factory, filters, null, (SearchEntryHandler[]) null);
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
  public Collection<Response<SearchResult>> search(
    final T factory,
    final String[] filters,
    final String... attrs)
    throws LdapException
  {
    final SearchFilter[] sf = new SearchFilter[filters.length];
    for (int i = 0; i < filters.length; i++) {
      sf[i] = new SearchFilter(filters[i]);
    }
    return search(factory, sf, attrs, (SearchEntryHandler[]) null);
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
  public Collection<Response<SearchResult>> search(
    final T factory,
    final SearchFilter[] filters,
    final String... attrs)
    throws LdapException
  {
    return search(factory, filters, attrs, (SearchEntryHandler[]) null);
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
  public abstract Collection<Response<SearchResult>> search(
    final T factory,
    final SearchFilter[] filters,
    final String[] attrs,
    final SearchEntryHandler... handlers)
    throws LdapException;
}
