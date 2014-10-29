/*
  $Id: AbstractServletSearchTemplatesExecutor.java 2885 2014-02-05 21:28:49Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2885 $
  Updated: $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
*/
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
import org.ldaptive.SearchResult;
import org.ldaptive.concurrent.AggregatePooledSearchExecutor;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.templates.Query;
import org.ldaptive.templates.SearchTemplates;
import org.ldaptive.templates.SearchTemplatesExecutor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Searches an LDAP using a defined set of search templates. For each term count
 * some number of templates are defined and used for searching.
 *
 * @author  Middleware Services
 * @version  $Revision $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractServletSearchTemplatesExecutor
  extends SearchTemplatesExecutor implements ServletSearchExecutor
{

  /** Spring context path. */
  private static final String SPRING_CONTEXT_PATH = "springContextPath";

  /** Default spring context path. */
  private static final String DEFAULT_SPRING_CONTEXT_PATH =
    "/templates-context.xml";

  /** Ignore pattern. */
  private static final String IGNORE_PATTERN = "ignorePattern";

  /** Regex pattern to ignore a query. */
  private Pattern ignorePattern;


  /** {@inheritDoc} */
  @Override
  public void initialize(final ServletConfig config)
  {
    final String springContextPath =
      config.getInitParameter(SPRING_CONTEXT_PATH) != null ?
        config.getInitParameter(SPRING_CONTEXT_PATH) :
        DEFAULT_SPRING_CONTEXT_PATH;
    logger.debug("{} = {}", SPRING_CONTEXT_PATH, springContextPath);

    final ApplicationContext context = new ClassPathXmlApplicationContext(
      springContextPath);
    setSearchExecutor(context.getBean(AggregatePooledSearchExecutor.class));
    logger.debug("searchExecutor = {}", getSearchExecutor());

    final Map<String, PooledConnectionFactory> factories =
      context.getBeansOfType(PooledConnectionFactory.class);
    setConnectionFactories(
      factories.values().toArray(
        new PooledConnectionFactory[factories.size()]));
    logger.debug(
      "connectionFactories = {}",
      Arrays.toString(getConnectionFactories()));

    final Map<String, SearchTemplates> templates = context.getBeansOfType(
      SearchTemplates.class);
    setSearchTemplates(
      templates.values().toArray(new SearchTemplates[templates.size()]));
    logger.debug("searchTemplates = {}", Arrays.toString(getSearchTemplates()));

    ignorePattern = config.getInitParameter(IGNORE_PATTERN) != null ?
      Pattern.compile(config.getInitParameter(IGNORE_PATTERN)) : null;
    logger.debug("{} = {}", IGNORE_PATTERN, ignorePattern);
  }


  /** {@inheritDoc} */
  @Override
  public void search(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws LdapException, IOException
  {
    Integer fromResult = null;
    if (request.getParameter("from-result") != null) {
      try {
        fromResult = Integer.valueOf(request.getParameter("from-result"));
      } catch (NumberFormatException e) {
        logger.warn(
          "Received invalid fromResult parameter: {}",
          request.getParameter("from-result"));
      }
    }

    Integer toResult = null;
    if (request.getParameter("to-result") != null) {
      try {
        toResult = Integer.valueOf(request.getParameter("to-result"));
      } catch (NumberFormatException e) {
        logger.warn(
          "Received invalid toResult parameter: {}",
          request.getParameter("to-result"));
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
      query.setReturnAttributes(request.getParameterValues("attrs"));
      query.setSearchRestrictions(request.getParameter("search-restrictions"));
      query.setFromResult(fromResult);
      query.setToResult(toResult);
      logger.info("Performing query {}", query);

      final SearchResult result = search(query);
      writeResponse(result, response);
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
  protected abstract void writeResponse(
    final SearchResult result,
    final HttpServletResponse response)
    throws IOException;
}
