/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.concurrent;

import java.util.Collection;
import org.ldaptive.FilterTemplate;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;

/**
 * Executes multiple ldap search operations asynchronously.
 *
 * @author  Middleware Services
 */
public class SearchOperationWorker extends AbstractOperationWorker<SearchOperation, SearchRequest, SearchResponse>
{


  /**
   * Default constructor.
   */
  public SearchOperationWorker()
  {
    super(new SearchOperation());
  }


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
   * Performs search operations for the supplied filters.
   *
   * @param  filters  to search with
   *
   * @return  search results
   */
  public Collection<SearchResponse> execute(final String... filters)
  {
    final FilterTemplate[] templates = new FilterTemplate[filters.length];
    for (int i = 0; i < filters.length; i++) {
      templates[i] = new FilterTemplate(filters[i]);
    }
    return execute(templates, (String[]) null);
  }


  /**
   * Performs search operations for the supplied filters.
   *
   * @param  templates  to search with
   *
   * @return  search results
   */
  public Collection<SearchResponse> execute(final FilterTemplate... templates)
  {
    return execute(templates, (String[]) null);
  }


  /**
   * Performs search operations for the supplied filters with the supplied return attributes
   *
   * @param  filters  to search with
   * @param  attrs  attributes to return
   *
   * @return  search results
   */
  public Collection<SearchResponse> execute(final String[] filters, final String... attrs)
  {
    final FilterTemplate[] templates = new FilterTemplate[filters.length];
    for (int i = 0; i < filters.length; i++) {
      templates[i] = new FilterTemplate(filters[i]);
    }
    return execute(templates, attrs);
  }


  /**
   * Performs search operations for the supplied filters with the supplied return attributes
   *
   * @param  templates  to search with
   * @param  attrs  attributes to return
   *
   * @return  search results
   */
  public Collection<SearchResponse> execute(final FilterTemplate[] templates, final String... attrs)
  {
    final SearchRequest[] requests = new SearchRequest[templates.length];
    for (int i = 0; i < templates.length; i++) {
      requests[i] = SearchRequest.copy(getOperation().getRequest());
      if (templates[i] != null) {
        requests[i].setFilter(templates[i]);
      }
      if (attrs != null) {
        requests[i].setReturnAttributes(attrs);
      }
    }
    return execute(requests);
  }
}
