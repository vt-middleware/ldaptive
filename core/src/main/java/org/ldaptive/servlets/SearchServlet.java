/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.util.Arrays;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queries an LDAP and returns the result in the servlet response. The following init params can be set for this
 * servlet:
 *
 * <ul>
 *   <li>servletSearchOperation - fully qualified class name that implements ServletSearchOperation</li>
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
 * <p>Example: http://www.server.com/Search?query=uid=dfisher If you need to pass complex queries, such as
 * (&amp;(cn=daniel*)(surname=fisher)), then the query must be form encoded. If you only want to receive a subset of
 * attributes those can be specified. Example:
 * http://www.server.com/Search?query=uid=dfisher&amp;attrs=givenname&amp;attrs=surname</p>
 *
 * @author  Middleware Services
 */
public final class SearchServlet extends HttpServlet
{

  /** Search operation implementation, value is {@value}. */
  private static final String SERVLET_SEARCH_OPERATION = "servletSearchOperation";

  /** serial version uid. */
  private static final long serialVersionUID = 3437252581014900696L;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Parses servlet requests and performs search operations. */
  private ServletSearchOperation searchOperation;


  @Override
  public void init(final ServletConfig config)
    throws ServletException
  {
    super.init(config);

    final String searchOperationClass = config.getInitParameter(SERVLET_SEARCH_OPERATION);
    if (searchOperationClass != null) {
      try {
        logger.debug("Creating servlet search operation: {}", searchOperationClass);
        searchOperation = (ServletSearchOperation) Class.forName(
          searchOperationClass).getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        logger.error("Error instantiating {}", searchOperationClass, e);
        throw new IllegalStateException(e);
      }
    } else {
      searchOperation = new LdifServletSearchOperation();
    }
    searchOperation.initialize(config);
  }


  @Override
  public void service(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException
  {
    logger.info(
      "search={} for attributes={}",
      request.getParameter("query"),
      Arrays.toString(request.getParameterValues("attrs")));
    try {
      searchOperation.execute(request, response);
    } catch (Exception e) {
      logger.error("Error performing search", e);
      throw new ServletException(e);
    }
  }


  @Override
  public void destroy()
  {
    try {
      searchOperation.close();
    } finally {
      super.destroy();
    }
  }
}
