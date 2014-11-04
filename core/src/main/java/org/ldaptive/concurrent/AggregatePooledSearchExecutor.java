/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
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
import org.ldaptive.Operation;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.pool.PooledConnectionFactory;

/**
 * Executes a list of search filters in parallel over a list of connection
 * factories, each search is performed on a separate connection in the pool. If
 * you need to execute all searches on the same connection see {@link
 * AggregateSearchExecutor}. A cached thread pool is used by default.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AggregatePooledSearchExecutor
  extends AbstractAggregateSearchExecutor<PooledConnectionFactory>
{


  /** Default constructor. */
  public AggregatePooledSearchExecutor()
  {
    this(Executors.newCachedThreadPool());
  }


  /**
   * Creates a new aggregate pooled search executor.
   *
   * @param  es  executor service
   */
  public AggregatePooledSearchExecutor(final ExecutorService es)
  {
    super(es);
  }


  /** {@inheritDoc} */
  @Override
  public Collection<Response<SearchResult>> search(
    final PooledConnectionFactory[] factories,
    final SearchFilter[] filters,
    final String[] attrs,
    final SearchEntryHandler... handlers)
    throws LdapException
  {
    final CompletionService<Response<SearchResult>> searches =
      new ExecutorCompletionService<>(getExecutorService());
    final List<Future<Response<SearchResult>>> futures =
      new ArrayList<>(factories.length * filters.length);
    for (ConnectionFactory factory : factories) {
      for (SearchFilter filter : filters) {
        final SearchRequest sr = newSearchRequest(this);
        if (filter != null) {
          sr.setSearchFilter(filter);
        }
        if (attrs != null) {
          sr.setReturnAttributes(attrs);
        }
        if (handlers != null) {
          sr.setSearchEntryHandlers(handlers);
        }

        final Connection conn = factory.getConnection();
        final SearchOperation op = createSearchOperation(conn);
        futures.add(searches.submit(createCallable(conn, op, sr)));
      }
    }

    final List<Response<SearchResult>> responses =
      new ArrayList<>(factories.length * filters.length);
    for (Future<Response<SearchResult>> future : futures) {
      try {
        responses.add(future.get());
      } catch (ExecutionException e) {
        logger.debug("ExecutionException thrown, ignoring", e);
      } catch (InterruptedException e) {
        logger.warn("InterruptedException thrown, ignoring", e);
      }
    }
    return responses;
  }


  /**
   * Returns a {@link Callable} that executes the supplied request with the
   * supplied operation in a try-finally block that opens and closes the
   * connection.
   *
   * @param  <Q>  type of ldap request
   * @param  <S>  type of ldap response
   * @param  conn  connection that the operation will execute on
   * @param  operation  to execute
   * @param  request  to pass to the operation
   *
   * @return  callable for the supplied operation and request
   */
  protected static <Q extends Request, S> Callable<Response<S>> createCallable(
    final Connection conn,
    final Operation<Q, S> operation,
    final Q request)
  {
    return
      new Callable<Response<S>>() {
      @Override
      public Response<S> call()
        throws LdapException
      {
        try {
          conn.open();
          return operation.execute(request);
        } finally {
          conn.close();
        }
      }
    };
  }
}
