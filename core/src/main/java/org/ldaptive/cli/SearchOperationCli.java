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
package org.ldaptive.cli;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.io.Dsmlv1Writer;
import org.ldaptive.io.LdifWriter;
import org.ldaptive.io.SearchResultWriter;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.props.SslConfigPropertySource;
import org.ldaptive.ssl.SslConfig;

/**
 * Command line interface for {@link SearchOperation}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SearchOperationCli extends AbstractCli
{

  /** option for dsmlv1 output. */
  private static final String OPT_DSMLV1 = "dsmlv1";

  /** name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapsearch";


  /**
   * CLI entry point method.
   *
   * @param  args  command line arguments.
   */
  public static void main(final String[] args)
  {
    final SearchOperationCli cli = new SearchOperationCli();
    final int status = cli.performAction(args);
    System.exit(status);
  }


  /** {@inheritDoc} */
  @Override
  protected void initOptions()
  {
    options.addOption(
      new Option(OPT_DSMLV1, false, "output results in DSML v1"));

    final Map<String, String> desc = getArgDesc(
      ConnectionConfig.class,
      SslConfig.class,
      SearchRequest.class);
    for (String s : ConnectionConfigPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : SslConfigPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : BindConnectionInitializerPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : SearchRequestPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    super.initOptions();
  }


  /**
   * Initialize a search request with command line options.
   *
   * @param  line  parsed command line arguments
   *
   * @return  search request that has been initialized
   */
  protected SearchRequest initSearchRequest(final CommandLine line)
  {
    final SearchRequest request = new SearchRequest();
    final SearchRequestPropertySource srSource =
      new SearchRequestPropertySource(
        request,
        getPropertiesFromOptions(PropertyDomain.LDAP.value(), line));
    srSource.initialize();
    return request;
  }


  /** {@inheritDoc} */
  @Override
  protected int dispatch(final CommandLine line)
    throws Exception
  {
    if (line.hasOption(OPT_DSMLV1)) {
      outputDsmlv1 = true;
    }
    if (line.hasOption(OPT_HELP)) {
      printHelp();
    } else {
      return search(initConnectionFactory(line), initSearchRequest(line));
    }
    return -1;
  }


  /**
   * Executes the ldap search operation.
   *
   * @param  cf  connection factory
   * @param  request  search request
   *
   * @return  status code
   *
   * @throws  Exception  on any LDAP search error
   */
  protected int search(final ConnectionFactory cf, final SearchRequest request)
    throws Exception
  {
    final Connection conn = cf.getConnection();
    conn.open();

    final SearchOperation op = new SearchOperation(conn);
    final Response<SearchResult> response = op.execute(request);
    final SearchResult result = response.getResult();
    SearchResultWriter writer;
    if (outputDsmlv1) {
      writer = new Dsmlv1Writer(
        new BufferedWriter(new OutputStreamWriter(System.out)));
    } else {
      writer = new LdifWriter(
        new BufferedWriter(new OutputStreamWriter(System.out)));
    }
    writer.write(result);
    conn.close();
    return response.getResultCode().value();
  }


  /** {@inheritDoc} */
  @Override
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
