/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cli;

import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ldaptive.CompareOperation;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapUtils;
import org.ldaptive.Response;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.SslConfigPropertySource;
import org.ldaptive.ssl.SslConfig;

/**
 * Command line interface for {@link CompareOperation}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class CompareOperationCli extends AbstractCli
{

  /** option for LDAP DN. */
  private static final String OPT_DN = "dn";

  /** option for LDAP attribute name/value pair. */
  private static final String OPT_ATTR = "attribute";

  /** name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapcompare";


  /**
   * CLI entry point method.
   *
   * @param  args  command line arguments.
   */
  public static void main(final String[] args)
  {
    final CompareOperationCli cli = new CompareOperationCli();
    final int status = cli.performAction(args);
    System.exit(status);
  }


  /** {@inheritDoc} */
  @Override
  protected void initOptions()
  {
    options.addOption(new Option(OPT_DN, true, "entry DN"));
    options.addOption(
      new Option(
        OPT_ATTR,
        true,
        "colon delimited name value pair (attr:value|attr::b64value)"));

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
      LdapAttribute la;
      final String[] attr = line.getOptionValue(OPT_ATTR).split(":", 2);
      if (attr[1].startsWith(":")) {
        la = new LdapAttribute(
          attr[0],
          LdapUtils.base64Decode(attr[1].substring(1)));
      } else {
        la = new LdapAttribute(attr[0], attr[1]);
      }
      return
        compare(initConnectionFactory(line), line.getOptionValue(OPT_DN), la);
    }
    return -1;
  }


  /**
   * Executes the ldap compare operation.
   *
   * @param  cf  connection factory
   * @param  dn  to compare attribute on
   * @param  attr  attribute to compare
   *
   * @return  status code
   *
   * @throws  Exception  on any LDAP search error
   */
  protected int compare(
    final ConnectionFactory cf,
    final String dn,
    final LdapAttribute attr)
    throws Exception
  {
    final Connection conn = cf.getConnection();
    conn.open();

    final CompareOperation op = new CompareOperation(conn);
    final Response<Boolean> response = op.execute(new CompareRequest(dn, attr));
    System.out.println(response.getResult());
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
