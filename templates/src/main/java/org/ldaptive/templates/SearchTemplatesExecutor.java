/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.Arrays;
import java.util.Collection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;
import org.ldaptive.concurrent.AggregatePooledSearchExecutor;
import org.ldaptive.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Searches an LDAP using a defined set of search templates. For each term count
 * some number of templates are defined and used for searching.
 *
 * @author  Middleware Services
 */
public class SearchTemplatesExecutor
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Search executor. */
  private AggregatePooledSearchExecutor searchExecutor;

  /** Connection factory. */
  private PooledConnectionFactory[] connectionFactories;

  /** Search templates. */
  private SearchTemplates[] searchTemplates;


  /** Default constructor. */
  public SearchTemplatesExecutor() {}


  /**
   * Creates a new templates search executor.
   *
   * @param  executor  aggregate pooled search executor
   * @param  factories  pooled connection factories
   * @param  templates  search templates
   */
  public SearchTemplatesExecutor(
    final AggregatePooledSearchExecutor executor,
    final PooledConnectionFactory[] factories,
    final SearchTemplates... templates)
  {
    searchExecutor = executor;
    connectionFactories = factories;
    searchTemplates = templates;
  }


  /**
   * Returns the search executor.
   *
   * @return  aggregate pooled search executor
   */
  public AggregatePooledSearchExecutor getSearchExecutor()
  {
    return searchExecutor;
  }


  /**
   * Sets the search executor.
   *
   * @param  executor  aggregate pooled search executor
   */
  public void setSearchExecutor(final AggregatePooledSearchExecutor executor)
  {
    searchExecutor = executor;
  }


  /**
   * Returns the connection factories.
   *
   * @return  pooled connection factories
   */
  public PooledConnectionFactory[] getConnectionFactories()
  {
    return connectionFactories;
  }


  /**
   * Sets the connection factories.
   *
   * @param  factories  pooled connection factory
   */
  public void setConnectionFactories(final PooledConnectionFactory[] factories)
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
   * Applies the supplied query to a search templates and aggregates all results
   * into a single search result.
   *
   * @param  query  to execute
   *
   * @return  ldap result
   *
   * @throws  LdapException  if the search fails
   */
  public SearchResult search(final Query query)
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
          logger.debug(
            "Found search templates {} for term count of {}",
            templates,
            termCount);
        } else {
          logger.debug(
            "No search module found for term count of {}",
            termCount);
        }
      } else {
        logger.debug("No terms found in query {}", query);
      }
    }

    if (templates != null) {
      return
        search(
          templates.format(query),
          query.getReturnAttributes(),
          query.getFromResult(),
          query.getToResult());
    } else {
      return null;
    }
  }


  /**
   * Performs an LDAP search with the supplied filters and aggregates all the
   * search results together.
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
  protected SearchResult search(
    final SearchFilter[] filters,
    final String[] returnAttrs,
    final Integer fromResult,
    final Integer toResult)
    throws LdapException
  {
    logger.debug("Performing search with {} filters", Arrays.toString(filters));

    // perform parallel searches
    final Collection<Response<SearchResult>> responses = searchExecutor.search(
      connectionFactories,
      filters,
      returnAttrs);

    // iterate over all results and store each entry
    final SearchResult result = new SearchResult(SortBehavior.ORDERED);
    for (Response<SearchResult> r : responses) {
      for (LdapEntry le : r.getResult().getEntries()) {
        result.addEntry(le);
        logger.debug("Search found: {}", le.getDn());
      }
    }

    SearchResult subResult;
    if (fromResult != null) {
      if (toResult != null) {
        subResult = result.subResult(fromResult, toResult);
      } else {
        subResult = result.subResult(fromResult, result.size());
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
    for (PooledConnectionFactory factory : connectionFactories) {
      factory.getConnectionPool().close();
    }
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::searchExecutor=%s, connectionFactories=%s, " +
        "searchTemplates=%s]",
        getClass().getName(),
        hashCode(),
        searchExecutor,
        Arrays.toString(connectionFactories),
        Arrays.toString(searchTemplates));
  }
}
