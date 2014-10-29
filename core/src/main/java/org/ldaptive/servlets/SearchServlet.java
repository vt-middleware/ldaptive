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
package org.ldaptive.servlets;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queries an LDAP and returns the result in the servlet response. The following
 * init params can be set for this servlet:
 *
 * <ul>
 *   <li>poolType - BLOCKING or SOFTLIMIT</li>
 *   <li>searchExecutorClass - fully qualified class name that implements
 *     ServletSearchExecutor</li>
 * </ul>
 *
 * <p>All other init params will set properties on:</p>
 *
 * <ul>
 *   <li>{@link org.ldaptive.SearchRequest}</li>
 *   <li>{@link org.ldaptive.ConnectionConfig}</li>
 *   <li>{@link org.ldaptive.pool.PoolConfig}</li>
 * </ul>
 *
 * <p>Example: http://www.server.com/Search?query=uid=dfisher If you need to
 * pass complex queries, such as (&(cn=daniel*)(surname=fisher)), then the query
 * must be form encoded. If you only want to receive a subset of attributes
 * those can be specified. Example:
 * http://www.server.com/Search?query=uid=dfisher&attrs=givenname&attrs=surname
 * </p>
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public final class SearchServlet extends HttpServlet
{

  /** Custom search executor implementation, value is {@value}. */
  private static final String SEARCH_EXECUTOR_CLASS = "searchExecutorClass";

  /** serial version uid. */
  private static final long serialVersionUID = 3437252581014900696L;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Parses servlet requests and performs search operations. */
  private ServletSearchExecutor searchExecutor;


  /** {@inheritDoc} */
  @Override
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    final String searchExecutorClass = config.getInitParameter(
      SEARCH_EXECUTOR_CLASS);
    if (searchExecutorClass != null) {
      try {
        logger.debug("Creating search executor: {}", searchExecutorClass);
        searchExecutor = (ServletSearchExecutor) Class.forName(
          searchExecutorClass).newInstance();
      } catch (Exception e) {
        logger.error("Error instantiating {}", searchExecutorClass, e);
        throw new IllegalStateException(e);
      }
    } else {
      searchExecutor = new Dsmlv1ServletSearchExecutor();
    }
    searchExecutor.initialize(config);
  }


  /** {@inheritDoc} */
  @Override
  public void service(
    final HttpServletRequest request,
    final HttpServletResponse response)
    throws ServletException, IOException
  {
    logger.info(
      "search={} for attributes={}",
      request.getParameter("query"),
      Arrays.toString(request.getParameterValues("attrs")));
    try {
      searchExecutor.search(request, response);
    } catch (Exception e) {
      logger.error("Error performing search", e);
      throw new ServletException(e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public void destroy()
  {
    try {
      searchExecutor.close();
    } finally {
      super.destroy();
    }
  }
}
