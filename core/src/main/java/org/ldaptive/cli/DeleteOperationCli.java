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

import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.SslConfigPropertySource;
import org.ldaptive.ssl.SslConfig;

/**
 * Command line interface for {@link DeleteOperation}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class DeleteOperationCli extends AbstractCli
{

  /** option for LDAP DN. */
  private static final String OPT_DN = "dn";

  /** name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapdelete";


  /**
   * CLI entry point method.
   *
   * @param  args  command line arguments.
   */
  public static void main(final String[] args)
  {
    final DeleteOperationCli cli = new DeleteOperationCli();
    final int status = cli.performAction(args);
    System.exit(status);
  }


  /** {@inheritDoc} */
  @Override
  protected void initOptions()
  {
    options.addOption(new Option(OPT_DN, true, "entry DN"));

    final Map<String, String> desc = getArgDesc(
      ConnectionConfig.class,
      SslConfig.class);
    for (String s : ConnectionConfigPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : SslConfigPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : BindConnectionInitializerPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    super.initOptions();
  }


  /** {@inheritDoc} */
  @Override
  protected int dispatch(final CommandLine line)
    throws Exception
  {
    if (line.hasOption(OPT_HELP)) {
      printHelp();
    } else {
      return delete(initConnectionFactory(line), line.getOptionValues(OPT_DN));
    }
    return -1;
  }


  /**
   * Executes the ldap delete operation.
   *
   * @param  cf  connection factory.
   * @param  entryDns  to delete
   *
   * @return  status code
   *
   * @throws  Exception  on any LDAP search error
   */
  protected int delete(final ConnectionFactory cf, final String[] entryDns)
    throws Exception
  {
    final Connection conn = cf.getConnection();
    conn.open();

    for (String dn : entryDns) {
      final DeleteOperation op = new DeleteOperation(conn);
      op.execute(new DeleteRequest(dn));
      System.out.println(String.format("Deleted entry: %s", dn));
    }
    conn.close();
    return 0;
  }


  /** {@inheritDoc} */
  @Override
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
