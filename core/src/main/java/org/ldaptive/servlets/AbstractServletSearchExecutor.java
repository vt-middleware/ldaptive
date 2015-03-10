/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.ldaptive.pool.ConnectionPoolType;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.props.PooledConnectionFactoryPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchRequestPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses {@link PooledConnectionFactory} and {@link SearchExecutor} to perform search operations. These objects are
 * configured from properties found in the servlet configuration.
 *
 * @author  Middleware Services
 */
public abstract class AbstractServletSearchExecutor implements ServletSearchExecutor
{

  /** Type of pool used, value is {@value}. */
  private static final String POOL_TYPE = "poolType";

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connections for searching. */
  private PooledConnectionFactory connectionFactory;

  /** Search executor for storing search properties. */
  private SearchExecutor searchExecutor;


  @Override
  public void initialize(final ServletConfig config)
  {
    searchExecutor = new SearchExecutor();

    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      searchExecutor,
      createProperties(config));
    srSource.initialize();
    logger.debug("searchExecutor = {}", searchExecutor);

    connectionFactory = new PooledConnectionFactory();

    final PooledConnectionFactoryPropertySource cfPropSource = new PooledConnectionFactoryPropertySource(
      connectionFactory,
      createProperties(config));
    cfPropSource.setPoolType(ConnectionPoolType.valueOf(config.getInitParameter(POOL_TYPE)));
    cfPropSource.initialize();
    logger.debug("connectionFactory = {}", connectionFactory);
  }


  /**
   * Returns context specific properties based on the supplied JAAS options.
   *
   * @param  config  to read properties from
   *
   * @return  properties
   */
  protected static Properties createProperties(final ServletConfig config)
  {
    final Properties p = new Properties();
    final Enumeration<?> e = config.getInitParameterNames();
    while (e.hasMoreElements()) {
      final String name = (String) e.nextElement();
      // if property name contains a dot, it isn't an ldaptive property
      // else add the domain to the ldaptive properties
      if (name.contains(".")) {
        p.setProperty(name, config.getInitParameter(name));
      } else {
        p.setProperty(PropertyDomain.LDAP.value() + name, config.getInitParameter(name));
      }
    }
    return p;
  }


  @Override
  public void search(final HttpServletRequest request, final HttpServletResponse response)
    throws LdapException, IOException
  {
    final String queryString = request.getParameter("query");
    if (queryString == null || queryString.isEmpty()) {
      logger.info("Ignoring empty query");
    } else {
      final SearchResult result = searchExecutor.search(
        connectionFactory,
        queryString,
        request.getParameterValues("attrs")).getResult();
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
  protected abstract void writeResponse(final SearchResult result, final HttpServletResponse response)
    throws IOException;


  @Override
  public void close()
  {
    connectionFactory.getConnectionPool().close();
  }
}
