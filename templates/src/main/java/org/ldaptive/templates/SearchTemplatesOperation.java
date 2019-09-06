/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.Arrays;
import java.util.Collection;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResponse;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches an LDAP using a defined set of search templates. For each term count some number of templates are defined
 * and used for searching.
 *
 * @author  Middleware Services
 */
public class SearchTemplatesOperation
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Search executor. */
  private SearchOperationWorker searchOperationWorker;

  /** Search templates. */
  private SearchTemplates[] searchTemplates;


  /** Default constructor. */
  public SearchTemplatesOperation() {}


  /**
   * Creates a new search templates operation.
   *
   * @param  worker  search operation worker
   * @param  templates  search templates
   */
  public SearchTemplatesOperation(final SearchOperationWorker worker, final SearchTemplates... templates)
  {
    searchOperationWorker = worker;
    searchTemplates = templates;
  }


  /**
   * Returns the search operation worker.
   *
   * @return  search operation worker
   */
  public SearchOperationWorker getSearchOperationWorker()
  {
    return searchOperationWorker;
  }


  /**
   * Sets the search operation worker.
   *
   * @param  worker  search operation worker
   */
  public void setSearchOperationWorker(final SearchOperationWorker worker)
  {
    searchOperationWorker = worker;
  }


  /**
   * Returns the search templates.
   *
   * @return  search templates
   */
  public SearchTemplates[] getSearchTemplates()
  {
    return searchTemplates;
  }


  /**
   * Sets the execute templates.
   *
   * @param  templates  execute templates
   */
  public void setSearchTemplates(final SearchTemplates[] templates)
  {
    searchTemplates = templates;
  }


  /**
   * Applies the supplied query to a search templates and aggregates all results into a single search result.
   *
   * @param  query  to execute
   *
   * @return  ldap result
   */
  public SearchResponse execute(final Query query)
  {
    logger.debug("Query: {}", query);

    SearchTemplates templates = null;
    if (query.getTerms().length > 0) {

      logger.debug("Processing query: {}", Arrays.toString(query.getTerms()));

      int termCount = query.getTerms().length;
      // if term count exceeds the highest defined templates
      // use the highest set of templates available
      if (termCount > searchTemplates.length) {
        termCount = searchTemplates.length;
      }
      if (termCount > 0) {
        templates = searchTemplates[termCount - 1];
        if (templates != null) {
          logger.debug("Found execute templates {} for term count of {}", templates, termCount);
        } else {
          logger.debug("No execute module found for term count of {}", termCount);
        }
      } else {
        logger.debug("No terms found in query {}", query);
      }
    }

    if (templates != null) {
      return execute(templates.format(query), query.getReturnAttributes(), query.getFromResult(), query.getToResult());
    } else {
      return null;
    }
  }


  /**
   * Performs an LDAP search with the supplied filters and aggregates all the search results together.
   *
   * @param  filters  to execute
   * @param  returnAttrs  attributes to return from the search
   * @param  fromResult  index to return results from
   * @param  toResult  index to return results to
   *
   * @return  ldap result containing all results
   */
  protected SearchResponse execute(
    final SearchFilter[] filters,
    final String[] returnAttrs,
    final Integer fromResult,
    final Integer toResult)
  {
    logger.debug("Performing search with {} filters", Arrays.toString(filters));

    // perform searches
    final Collection<SearchResponse> responses = searchOperationWorker.execute(filters, returnAttrs);

    // iterate over all results and store each entry
    final SearchResponse result = new SearchResponse();
    for (SearchResponse res : responses) {
      for (LdapEntry e : res.getEntries()) {
        result.addEntries(e);
        logger.debug("Search found: {}", e.getDn());
      }
    }

    final SearchResponse subResult;
    if (fromResult != null) {
      if (toResult != null) {
        subResult = result.subResult(fromResult, toResult);
      } else {
        subResult = result.subResult(fromResult, result.entrySize());
      }
    } else if (toResult != null) {
      subResult = result.subResult(0, toResult);
    } else {
      subResult = result;
    }
    return subResult;
  }


  /** Closes any resources associated with this object. */
  public void close()
  {
    if (searchOperationWorker != null) {
      searchOperationWorker.getOperation().getConnectionFactory().close();
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("searchOperationWorker=").append(searchOperationWorker).append(", ")
      .append("searchTemplates=").append(Arrays.toString(searchTemplates)).append("]").toString();
  }
}
