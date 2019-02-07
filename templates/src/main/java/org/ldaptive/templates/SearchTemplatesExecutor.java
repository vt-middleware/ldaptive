/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.Arrays;
import java.util.Collection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResponse;
import org.ldaptive.concurrent.AggregateSearchOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches an LDAP using a defined set of search templates. For each term count some number of templates are defined
 * and used for searching.
 *
 * @author  Middleware Services
 */
public class SearchTemplatesExecutor
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Search executor. */
  private AggregateSearchOperation searchOperation;

  /** Connection factory. */
  private ConnectionFactory[] connectionFactories;

  /** Search templates. */
  private SearchTemplates[] searchTemplates;


  /** Default constructor. */
  public SearchTemplatesExecutor() {}


  /**
   * Creates a new templates search executor.
   *
   * @param  operation  aggregate pooled search executor
   * @param  factories  pooled connection factories
   * @param  templates  search templates
   */
  public SearchTemplatesExecutor(
    final AggregateSearchOperation operation,
    final PooledConnectionFactory[] factories,
    final SearchTemplates... templates)
  {
    searchOperation = operation;
    connectionFactories = factories;
    searchTemplates = templates;
  }


  /**
   * Returns the search operation.
   *
   * @return  aggregate search operation
   */
  public AggregateSearchOperation getSearchOperation()
  {
    return searchOperation;
  }


  /**
   * Sets the search operation.
   *
   * @param  operation  aggregate search operation
   */
  public void setSearchOperation(final AggregateSearchOperation operation)
  {
    searchOperation = operation;
  }


  /**
   * Returns the connection factories.
   *
   * @return  connection factories
   */
  public ConnectionFactory[] getConnectionFactories()
  {
    return connectionFactories;
  }


  /**
   * Sets the connection factories.
   *
   * @param  factories  connection factory
   */
  public void setConnectionFactories(final ConnectionFactory[] factories)
  {
    connectionFactories = factories;
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
   * Sets the search templates.
   *
   * @param  templates  search templates
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
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResponse search(final Query query)
    throws LdapException
  {
    logger.debug("Query: {}", query);

    // get a search object
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
          logger.debug("Found search templates {} for term count of {}", templates, termCount);
        } else {
          logger.debug("No search module found for term count of {}", termCount);
        }
      } else {
        logger.debug("No terms found in query {}", query);
      }
    }

    if (templates != null) {
      return search(templates.format(query), query.getReturnAttributes(), query.getFromResult(), query.getToResult());
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
   *
   * @throws  LdapException  if the search fails
   */
  protected SearchResponse search(
    final SearchFilter[] filters,
    final String[] returnAttrs,
    final Integer fromResult,
    final Integer toResult)
    throws LdapException
  {
    logger.debug("Performing search with {} filters", Arrays.toString(filters));

    // perform searches
    final Collection<SearchResponse> responses = searchOperation.execute(
      connectionFactories,
      filters,
      returnAttrs);

    // iterate over all results and store each entry
    final SearchResponse result = new SearchResponse();
    for (SearchResponse res : responses) {
      for (LdapEntry e : res.getEntries()) {
        result.addEntry(e);
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
    if (connectionFactories != null) {
      for (ConnectionFactory factory : connectionFactories) {
        factory.close();
      }
    }
    if (searchOperation != null) {
      searchOperation.shutdown();
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("searchOperation=").append(searchOperation).append(", ")
      .append("connectionFactories=").append(Arrays.toString(connectionFactories)).append(", ")
      .append("searchTemplates=").append(Arrays.toString(searchTemplates)).append("]").toString();
  }
}
