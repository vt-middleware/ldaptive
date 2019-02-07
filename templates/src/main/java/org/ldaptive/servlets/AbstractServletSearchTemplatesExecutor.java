/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.LdapException;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchResponse;
import org.ldaptive.concurrent.AggregateSearchOperation;
import org.ldaptive.templates.Query;
import org.ldaptive.templates.SearchTemplates;
import org.ldaptive.templates.SearchTemplatesExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Searches an LDAP using a defined set of search templates. For each term count some number of templates are defined
 * and used for searching.
 *
 * @author  Middleware Services
 */
public abstract class AbstractServletSearchTemplatesExecutor extends SearchTemplatesExecutor
  implements ServletSearchExecutor
{

  /** Spring context path. */
  private static final String SPRING_CONTEXT_PATH = "springContextPath";

  /** Default spring context path. */
  private static final String DEFAULT_SPRING_CONTEXT_PATH = "/templates-context.xml";

  /** Ignore pattern. */
  private static final String IGNORE_PATTERN = "ignorePattern";

  /** Minimum query term length. */
  private static final String MINIMUM_QUERY_TERM_LENGTH = "minimumQueryTermLength";

  /** Regex pattern to ignore a query. */
  private Pattern ignorePattern;

  /** Minimum length for at least one query term. */
  private int minimumQueryTermLength;


  @Override
  public void initialize(final ServletConfig config)
  {
    final String springContextPath = config.getInitParameter(SPRING_CONTEXT_PATH) != null
      ? config.getInitParameter(SPRING_CONTEXT_PATH) : DEFAULT_SPRING_CONTEXT_PATH;
    logger.debug("{} = {}", SPRING_CONTEXT_PATH, springContextPath);

    final ApplicationContext context = new ClassPathXmlApplicationContext(springContextPath);
    setSearchOperation(context.getBean(AggregateSearchOperation.class));
    logger.debug("searchExecutor = {}", getSearchOperation());

    final Map<String, PooledConnectionFactory> factories = context.getBeansOfType(PooledConnectionFactory.class);
    setConnectionFactories(factories.values().toArray(new PooledConnectionFactory[0]));
    logger.debug("connectionFactories = {}", Arrays.toString(getConnectionFactories()));

    final Map<String, SearchTemplates> templates = context.getBeansOfType(SearchTemplates.class);
    setSearchTemplates(templates.values().toArray(new SearchTemplates[0]));
    logger.debug("searchTemplates = {}", Arrays.toString(getSearchTemplates()));

    ignorePattern = config.getInitParameter(IGNORE_PATTERN) != null
      ? Pattern.compile(config.getInitParameter(IGNORE_PATTERN)) : null;
    logger.debug("{} = {}", IGNORE_PATTERN, ignorePattern);

    minimumQueryTermLength = config.getInitParameter(MINIMUM_QUERY_TERM_LENGTH) != null
      ? Integer.parseInt(config.getInitParameter(MINIMUM_QUERY_TERM_LENGTH)) : 0;
    logger.debug("{} = {}", MINIMUM_QUERY_TERM_LENGTH, minimumQueryTermLength);
  }


  @Override
  public void search(final HttpServletRequest request, final HttpServletResponse response)
    throws LdapException, IOException
  {
    Integer fromResult = null;
    if (request.getParameter("from-result") != null) {
      try {
        fromResult = Integer.valueOf(request.getParameter("from-result"));
      } catch (NumberFormatException e) {
        logger.warn("Received invalid fromResult parameter: {}", request.getParameter("from-result"));
      }
    }

    Integer toResult = null;
    if (request.getParameter("to-result") != null) {
      try {
        toResult = Integer.valueOf(request.getParameter("to-result"));
      } catch (NumberFormatException e) {
        logger.warn("Received invalid toResult parameter: {}", request.getParameter("to-result"));
      }
    }

    boolean doSearch = true;
    final String queryString = request.getParameter("query");
    if (queryString == null || queryString.length() == 0) {
      logger.info("Ignoring empty query");
      doSearch = false;
    }
    if (doSearch && ignorePattern != null) {
      final Matcher matcher = ignorePattern.matcher(queryString);
      if (matcher.matches()) {
        logger.info("Ignoring query {}", queryString);
        doSearch = false;
      }
    }
    if (doSearch) {
      final Query query = new Query(queryString);
      if (minimumQueryTermLength > 0) {
        boolean hasMinimumLengthQueryTerm = false;
        for (String term : query.getTerms()) {
          if (term.length() >= minimumQueryTermLength) {
            hasMinimumLengthQueryTerm = true;
            break;
          }
        }
        if (!hasMinimumLengthQueryTerm) {
          logger.info("Does not meet minimum query term length {}", queryString);
          doSearch = false;
        }
      }
      if (doSearch) {
        query.setReturnAttributes(request.getParameterValues("attrs"));
        query.setSearchRestrictions(request.getParameter("search-restrictions"));
        query.setFromResult(fromResult);
        query.setToResult(toResult);
        logger.info("Performing query {}", query);

        final SearchResponse result = search(query);
        writeResponse(result, response);
      }
    }
  }


  /**
   * Writes the supplied search result to the servlet response output stream.
   *
   * @param  result  search result to write
   * @param  response  to write to
   *
   * @throws  IOException  if an error occurs writing to the response
   */
  protected abstract void writeResponse(SearchResponse result, HttpServletResponse response)
    throws IOException;
}
