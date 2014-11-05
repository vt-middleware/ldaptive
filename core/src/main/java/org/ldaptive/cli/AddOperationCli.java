/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cli;

import java.io.FileReader;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchResult;
import org.ldaptive.io.LdifReader;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.SslConfigPropertySource;
import org.ldaptive.ssl.SslConfig;

/**
 * Command line interface for {@link AddOperation}.
 *
 * @author  Middleware Services
 */
public class AddOperationCli extends AbstractCli
{

  /** option for LDIF file. */
  private static final String OPT_FILE = "file";

  /** name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapadd";


  /**
   * CLI entry point method.
   *
   * @param  args  command line arguments.
   */
  public static void main(final String[] args)
  {
    final AddOperationCli cli = new AddOperationCli();
    final int status = cli.performAction(args);
    System.exit(status);
  }


  /** {@inheritDoc} */
  @Override
  protected void initOptions()
  {
    options.addOption(new Option(OPT_FILE, true, "LDIF file"));

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
      return add(initConnectionFactory(line), line.getOptionValue(OPT_FILE));
    }
    return -1;
  }


  /**
   * Executes the ldap add operation.
   *
   * @param  cf  connection factory
   * @param  file  to read ldif from
   *
   * @return  status code
   *
   * @throws  Exception  on any LDAP search error
   */
  protected int add(final ConnectionFactory cf, final String file)
    throws Exception
  {
    final Connection conn = cf.getConnection();
    conn.open();

    final LdifReader reader = new LdifReader(new FileReader(file));
    final SearchResult sr = reader.read();
    for (LdapEntry le : sr.getEntries()) {
      final AddOperation op = new AddOperation(conn);
      op.execute(new AddRequest(le.getDn(), le.getAttributes()));
      System.out.println(String.format("Added entry: %s", le));
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
