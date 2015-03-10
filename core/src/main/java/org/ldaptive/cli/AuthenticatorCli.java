/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.cli;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.LdapEntry;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchResult;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.SearchDnResolver;
import org.ldaptive.io.Dsmlv1Writer;
import org.ldaptive.io.LdifWriter;
import org.ldaptive.io.SearchResultWriter;
import org.ldaptive.props.AuthenticationRequestPropertySource;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.PropertySource.PropertyDomain;
import org.ldaptive.props.SearchDnResolverPropertySource;

/**
 * Command line interface for {@link Authenticator}.
 *
 * @author  Middleware Services
 */
public class AuthenticatorCli extends AbstractCli
{

  /** option for dsmlv1 output. */
  private static final String OPT_DSMLV1 = "dsmlv1";

  /** name of operation provided by this class. */
  private static final String COMMAND_NAME = "ldapauth";


  /**
   * CLI entry point method.
   *
   * @param  args  command line arguments.
   */
  public static void main(final String[] args)
  {
    final AuthenticatorCli cli = new AuthenticatorCli();
    final int status = cli.performAction(args);
    System.exit(status);
  }


  @Override
  protected void initOptions()
  {
    options.addOption(new Option(OPT_DSMLV1, false, "output results in DSML v1"));

    final Map<String, String> desc = getArgDesc(
      ConnectionConfig.class,
      Authenticator.class,
      SearchDnResolver.class,
      AuthenticationRequest.class);
    for (String s : ConnectionConfigPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : BindConnectionInitializerPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : AuthenticatorPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    for (String s : SearchDnResolverPropertySource.getProperties()) {
      // ignore core search related properties
      if (!s.startsWith("search")) {
        options.addOption(new Option(s, true, desc.get(s)));
      }
    }
    for (String s : AuthenticationRequestPropertySource.getProperties()) {
      options.addOption(new Option(s, true, desc.get(s)));
    }
    super.initOptions();
  }


  /**
   * Initialize an Authenticator with command line options.
   *
   * @param  line  parsed command line arguments
   *
   * @return  authenticator that has been initialized
   */
  protected Authenticator initAuthenticator(final CommandLine line)
  {
    final Authenticator auth = new Authenticator();
    final AuthenticatorPropertySource aSource = new AuthenticatorPropertySource(
      auth,
      getPropertiesFromOptions(PropertyDomain.AUTH.value(), line));
    aSource.initialize();
    return auth;
  }


  /**
   * Initialize an authentication request with command line options.
   *
   * @param  line  parsed command line arguments
   *
   * @return  authentication request that has been initialized
   */
  protected AuthenticationRequest initAuthenticationRequest(final CommandLine line)
  {
    final AuthenticationRequest request = new AuthenticationRequest();
    final AuthenticationRequestPropertySource arSource = new AuthenticationRequestPropertySource(
      request,
      getPropertiesFromOptions(PropertyDomain.AUTH.value(), line));
    arSource.initialize();
    if (request.getUser() == null) {
      // prompt for a user name
      final String user = System.console().readLine("[Enter user name]: ");
      request.setUser(user);
    }

    if (request.getCredential() == null) {
      // prompt the user to enter a password
      final char[] pass = System.console().readPassword("[Enter password for %s]: ", request.getUser());
      request.setCredential(new Credential(pass));
    }

    return request;
  }


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
      return authenticate(initAuthenticator(line), initAuthenticationRequest(line));
    }
    return -1;
  }


  /**
   * Executes the authentication operation.
   *
   * @param  auth  authenticator
   * @param  request  authentication request
   *
   * @return  status code
   *
   * @throws  Exception  on any LDAP error
   */
  protected int authenticate(final Authenticator auth, final AuthenticationRequest request)
    throws Exception
  {
    // by default return all attributes
    if (request.getReturnAttributes() != null && request.getReturnAttributes().length == 0) {
      request.setReturnAttributes(ReturnAttributes.ALL_USER.value());
    }

    final AuthenticationResponse response = auth.authenticate(request);
    final LdapEntry entry = response.getLdapEntry();
    if (response.getResult()) {
      if (entry != null) {
        SearchResultWriter writer;
        if (outputDsmlv1) {
          writer = new Dsmlv1Writer(new BufferedWriter(new OutputStreamWriter(System.out)));
        } else {
          writer = new LdifWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
        }
        writer.write(new SearchResult(entry));
      }
    } else {
      System.out.println(String.format("Authentication failed for %s", entry));
    }
    return response.getResultCode().value();
  }


  @Override
  protected String getCommandName()
  {
    return COMMAND_NAME;
  }
}
