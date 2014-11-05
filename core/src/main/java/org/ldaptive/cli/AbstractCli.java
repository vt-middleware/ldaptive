/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.props.DefaultConnectionFactoryPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;

/**
 * Abstract base class for all CLI classes.
 *
 * @author  Middleware Services
 * @version  $Revision: 3066 $ $Date: 2014-10-16 12:23:27 -0400 (Thu, 16 Oct 2014) $
 */
public abstract class AbstractCli
{

  /** option to print usage. */
  protected static final String OPT_HELP = "help";

  /** option for provider properties. */
  protected static final String OPT_PROVIDER_PROPERTIES = "providerProperties";

  /** command line options. */
  protected final Options options = new Options();

  /** whether to output dsml version 1, the default is ldif. */
  protected boolean outputDsmlv1;


  /**
   * Parses command line options and dispatches to the requested action, or the
   * default action if no action is specified.
   *
   * @param  args  command line arguments
   *
   * @return  status code
   */
  public final int performAction(final String[] args)
  {
    initOptions();

    int status = -1;
    try {
      if (args.length > 0) {
        final CommandLineParser parser = new GnuParser();
        final CommandLine line = parser.parse(options, args, false);
        status = dispatch(line);
      } else {
        printExamples();
      }
    } catch (ParseException pex) {
      System.err.println(
        "Failed parsing command arguments: " + pex.getMessage());
    } catch (IllegalArgumentException iaex) {
      String msg = "Operation failed: " + iaex.getMessage();
      if (iaex.getCause() != null) {
        msg += " Underlying reason: " + iaex.getCause().getMessage();
      }
      System.err.println(msg);
    } catch (RuntimeException rex) {
      throw rex;
    } catch (LdapException ex) {
      System.err.println("LDAP Operation failed:");
      ex.printStackTrace(System.err);
      if (ex.getResultCode() != null) {
        status = ex.getResultCode().value();
      }
    } catch (Exception ex) {
      System.err.println("Operation failed:");
      ex.printStackTrace(System.err);
    }
    return status;
  }


  /** Initialize CLI options. */
  protected void initOptions()
  {
    options.addOption(new Option(OPT_HELP, false, "display all options"));
    options.addOption(
      new Option(
        OPT_PROVIDER_PROPERTIES,
        true,
        "provider specific properties"));
  }


  /**
   * Initialize a connection factory with command line options.
   *
   * @param  line  parsed command line arguments
   *
   * @return  connection factory that has been initialized
   */
  protected ConnectionFactory initConnectionFactory(final CommandLine line)
  {
    final DefaultConnectionFactory factory = new DefaultConnectionFactory();
    final DefaultConnectionFactoryPropertySource cfSource =
      new DefaultConnectionFactoryPropertySource(
        factory,
        getPropertiesFromOptions(PropertyDomain.LDAP.value(), line));
    cfSource.initialize();

    final ConnectionInitializer ci =
      factory.getConnectionConfig().getConnectionInitializer();
    if (ci instanceof BindConnectionInitializer) {
      final BindConnectionInitializer bci = (BindConnectionInitializer) ci;
      if (bci.getBindDn() != null && bci.getBindCredential() == null) {
        // prompt the user to enter a password
        final char[] pass = System.console().readPassword(
          "[Enter password for %s]: ",
          bci.getBindDn());
        bci.setBindCredential(new Credential(pass));
      }
    }
    return factory;
  }


  /**
   * Returns the name of the command for which this class provides a CLI
   * interface.
   *
   * @return  name of CLI command
   */
  protected abstract String getCommandName();


  /**
   * Dispatch command line data to the active that can perform the operation
   * requested on the command line.
   *
   * @param  line  parsed command line arguments
   *
   * @return  status code
   *
   * @throws  Exception  on errors thrown by action
   */
  protected abstract int dispatch(final CommandLine line)
    throws Exception;


  /** Prints CLI help text. */
  protected void printHelp()
  {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(getCommandName(), options);
  }


  /** Prints CLI usage examples. */
  protected void printExamples()
  {
    final String name = getClass().getSimpleName();
    final InputStream in = getClass().getResourceAsStream(name + ".examples");
    if (in != null) {
      final BufferedReader reader = new BufferedReader(
        new InputStreamReader(in));
      try {
        System.out.println();

        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      } catch (IOException e) {
        System.err.println("Error reading examples from resource stream.");
      } finally {
        try {
          reader.close();
        } catch (IOException ex) {
          System.err.println("Error closing example resource stream.");
        }
        System.out.println();
      }
    } else {
      System.out.println("No usage examples available for " + getCommandName());
    }
  }


  /**
   * Returns the command line argument descriptions for this CLI.
   *
   * @param  classes  that contain arguments used by this CLI
   *
   * @return  map of argument name to description
   */
  protected Map<String, String> getArgDesc(final Class<?>... classes)
  {
    final Map<String, String> args = new HashMap<>();
    for (Class<?> c : classes) {
      final String name = c.getSimpleName();
      final InputStream in = getClass().getResourceAsStream(name + ".args");
      if (in != null) {
        final BufferedReader reader = new BufferedReader(
          new InputStreamReader(in));
        try {
          String line;
          while ((line = reader.readLine()) != null) {
            final String[] s = line.split(":");
            if (s.length > 1) {
              args.put(s[0], s[1]);
            }
          }
        } catch (IOException e) {
          System.err.println("Error reading arguments from resource stream.");
        } finally {
          try {
            reader.close();
          } catch (IOException ex) {
            System.err.println("Error closing arguments resource stream.");
          }
        }
      }
    }
    if (args.isEmpty()) {
      System.err.println("No arguments available for " + getCommandName());
    }
    return args;
  }


  /**
   * Reads the options from the supplied command line and returns a properties
   * containing those options.
   *
   * @param  domain  to place property names in
   * @param  line  command line
   *
   * @return  properties for each option and value
   */
  protected Properties getPropertiesFromOptions(
    final String domain,
    final CommandLine line)
  {
    final Properties props = new Properties();
    for (Option o : line.getOptions()) {
      if (o.hasArg()) {
        // if provider property, split the value
        // else add the domain to the ldaptive properties
        if (o.getOpt().equals(OPT_PROVIDER_PROPERTIES)) {
          final String[] s = o.getValue().split("=");
          props.setProperty(s[0], s[1]);
        } else {
          props.setProperty(domain + o.getOpt(), o.getValue());
        }
      }
    }
    return props;
  }
}
