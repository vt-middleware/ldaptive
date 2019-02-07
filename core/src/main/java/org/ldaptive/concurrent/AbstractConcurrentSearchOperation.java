/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.concurrent.ExecutorService;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.handler.LdapEntryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for concurrent search executors.
 *
 * @author  Middleware Services
 */
public abstract class AbstractConcurrentSearchOperation extends SearchOperation
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** To submit operations to. */
  private final ExecutorService service;


  /**
   * Creates a new abstract search executor.
   *
   * @param  es  executor service
   */
  public AbstractConcurrentSearchOperation(final ExecutorService es)
  {
    if (es == null) {
      throw new NullPointerException("ExecutorService cannot be null");
    }
    service = es;
  }


  /**
   * Returns the executor service for this search executor.
   *
   * @return  executor service
   */
  protected ExecutorService getExecutorService()
  {
    return service;
  }


  /** Shuts down the executor service. See {@link ExecutorService#shutdown()}. */
  public void shutdown()
  {
    service.shutdown();
  }


  /**
   * Creates an array of search requests that contain the supplied filters and return attributes.
   *
   * @param  filters  to search for
   * @param  attrs  attributes to return
   *
   * @return  search requests
   */
  protected SearchRequest[] createSearchRequests(final SearchFilter[] filters, final String[] attrs)
  {
    final SearchRequest[] requests = new SearchRequest[filters.length];
    for (int i = 0; i < filters.length; i++) {
      requests[i] = SearchRequest.copy(getRequest());
      if (filters[i] != null) {
        requests[i].setFilter(filters[i]);
      }
      if (attrs != null) {
        requests[i].setReturnAttributes(attrs);
      }
    }
    return requests;
  }


  /**
   * Creates a new search operation configured with the properties on this search executor.
   *
   * @param  cf  to get connections from
   * @param  handlers  entry handlers
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation(
    final ConnectionFactory cf,
    final LdapEntryHandler... handlers)
  {
    final SearchOperation op = SearchOperation.copy(this);
    op.setConnectionFactory(cf);
    if (handlers != null) {
      op.setEntryHandlers(handlers);
    }
    return op;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("service=").append(service).append("]").toString();
  }
}
