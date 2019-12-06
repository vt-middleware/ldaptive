/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.servlets;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ldaptive.LdapException;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.props.PooledConnectionFactoryPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchRequestPropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses {@link PooledConnectionFactory} and {@link SearchOperation} to perform search operations. These objects are
 * configured from properties found in the servlet configuration.
 *
 * @author  Middleware Services
 */
public abstract class AbstractServletSearchOperation implements ServletSearchOperation
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connections for searching. */
  private PooledConnectionFactory connectionFactory;


  @Override
  public void initialize(final ServletConfig config)
  {
    final SearchRequest searchRequest = new SearchRequest();
    final SearchRequestPropertySource srSource = new SearchRequestPropertySource(
      searchRequest,
      createProperties(config));
    srSource.initialize();
    logger.debug("searchRequest = {}", searchRequest);

    connectionFactory = new PooledConnectionFactory();

    final PooledConnectionFactoryPropertySource cfPropSource = new PooledConnectionFactoryPropertySource(
      connectionFactory,
      createProperties(config));
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
  public void execute(final HttpServletRequest request, final HttpServletResponse response)
    throws LdapException, IOException
  {
    final String queryString = request.getParameter("query");
    if (queryString == null || queryString.isEmpty()) {
      logger.info("Ignoring empty query");
    } else {
      final SearchOperation search = new SearchOperation(connectionFactory);
      final SearchResponse result = search.execute(
        queryString,
        request.getParameterValues("attrs"));
      writeResponse(result, response);
    }
  }


  /**
   * Writes the supplied execute result to the servlet response output stream.
   *
   * @param  result  execute result to write
   * @param  response  to write to
   *
   * @throws  IOException  if an error occurs writing to the response
   */
  protected abstract void writeResponse(SearchResponse result, HttpServletResponse response)
    throws IOException;


  @Override
  public void close()
  {
    connectionFactory.close();
  }
}
