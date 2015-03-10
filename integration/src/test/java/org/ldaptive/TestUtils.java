/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.NoOpDnResolver;
import org.ldaptive.io.LdifReader;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;

/**
 * Utility methods for ldap tests.
 *
 * @author  Middleware Services
 */
public final class TestUtils
{

  /** Location of the hostname in the output of netstat. */
  public static final int NETSTAT_HOST_INDEX = 4;


  /** Default constructor. */
  private TestUtils() {}


  /**
   * @param  path  to read properties from, if null use default properties
   *
   * @return  connection config
   */
  public static ConnectionConfig readConnectionConfig(final String path)
  {
    final ConnectionConfig cc = new ConnectionConfig();
    ConnectionConfigPropertySource ccSource = null;
    if (path != null) {
      ccSource = new ConnectionConfigPropertySource(cc, path);
    } else {
      ccSource = new ConnectionConfigPropertySource(cc);
    }
    ccSource.initialize();
    return cc;
  }


  /**
   * @param  path  to read properties from, if null use default properties
   *
   * @return  authenticator
   */
  public static Authenticator readAuthenticator(final String path)
  {
    final Authenticator a = new Authenticator();
    AuthenticatorPropertySource aSource = null;
    if (path != null) {
      aSource = new AuthenticatorPropertySource(a, path);
    } else {
      aSource = new AuthenticatorPropertySource(a);
    }
    aSource.initialize();
    return a;
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "setup-ldap")
  public static Connection createSetupConnection()
    throws Exception
  {
    return
      DefaultConnectionFactory.getConnection(readConnectionConfig("classpath:/org/ldaptive/ldap.setup.properties"));
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "ldap")
  public static Connection createConnection()
    throws Exception
  {
    return DefaultConnectionFactory.getConnection(readConnectionConfig(null));
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "sasl-external-ldap")
  public static Connection createSaslExternalConnection()
    throws Exception
  {
    return
      DefaultConnectionFactory.getConnection(readConnectionConfig("classpath:/org/ldaptive/ldap.external.properties"));
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "digest-md5-ldap")
  public static Connection createDigestMd5Connection()
    throws Exception
  {
    return
      DefaultConnectionFactory.getConnection(
        readConnectionConfig("classpath:/org/ldaptive/ldap.digest-md5.properties"));
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "cram-md5-ldap")
  public static Connection createCramMd5Connection()
    throws Exception
  {
    return
      DefaultConnectionFactory.getConnection(readConnectionConfig("classpath:/org/ldaptive/ldap.cram-md5.properties"));
  }


  /**
   * @return  connection
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "gss-api-ldap")
  public static Connection createGssApiConnection()
    throws Exception
  {
    return
      DefaultConnectionFactory.getConnection(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi.properties"));
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "ssl-auth")
  public static Authenticator createSSLAuthenticator()
    throws Exception
  {
    return readAuthenticator("classpath:/org/ldaptive/ldap.ssl.properties");
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "ssl-dn-auth")
  public static Authenticator createSSLDnAuthenticator()
    throws Exception
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.ssl.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "tls-auth")
  public static Authenticator createTLSAuthenticator()
    throws Exception
  {
    return readAuthenticator("classpath:/org/ldaptive/ldap.tls.properties");
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "tls-dn-auth")
  public static Authenticator createTLSDnAuthenticator()
    throws Exception
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.tls.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "digest-md5-auth")
  public static Authenticator createDigestMD5Authenticator()
    throws Exception
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.digest-md5.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * @return  authenticator
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "cram-md5-auth")
  public static Authenticator createCramMD5Authenticator()
    throws Exception
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.cram-md5.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * Reads a file on the classpath into a reader.
   *
   * @param  filename  to open.
   *
   * @return  reader.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static BufferedReader readFile(final String filename)
    throws Exception
  {
    return new BufferedReader(new InputStreamReader(TestUtils.class.getResourceAsStream(filename)));
  }


  /**
   * Reads a file on the classpath into a string.
   *
   * @param  filename  to open.
   *
   * @return  string.
   *
   * @throws  Exception  If file cannot be read.
   */
  public static String readFileIntoString(final String filename)
    throws Exception
  {
    final StringBuilder result = new StringBuilder();
    try (BufferedReader br = readFile(filename)) {
      String line;
      while ((line = br.readLine()) != null) {
        result.append(line).append(System.getProperty("line.separator"));
      }
    }
    return result.toString();
  }


  /**
   * Converts an ldif to a ldap result.
   *
   * @param  ldif  to convert.
   *
   * @return  ldap result.
   *
   * @throws  Exception  if ldif cannot be read
   */
  public static SearchResult convertLdifToResult(final String ldif)
    throws Exception
  {
    final LdifReader reader = new LdifReader(new StringReader(ldif));
    return reader.read();
  }


  /**
   * Converts a string of the form: givenName=John|sn=Doe into a ldap attributes and stores them in an ldap entry.
   *
   * @param  dn  of the entry
   * @param  attrs  to convert.
   *
   * @return  ldap entry with attributes but no dn.
   */
  public static LdapEntry convertStringToEntry(final String dn, final String attrs)
  {
    final LdapEntry le = new LdapEntry(dn);
    for (String s : attrs.split("\\|")) {
      final String[] nameValuePairs = s.trim().split("=", 2);
      if (le.getAttribute(nameValuePairs[0]) != null) {
        le.getAttribute(nameValuePairs[0]).addStringValue(nameValuePairs[1]);
      } else {
        le.addAttribute(new LdapAttribute(nameValuePairs[0], nameValuePairs[1]));
      }
    }
    return le;
  }


  /**
   * Invokes {@link AssertJUnit#assertEquals(Object, Object)} after converting the entries in actual from SearchEntry to
   * LdapEntry.
   *
   * @param  expected  value
   * @param  actual  value
   */
  public static void assertEquals(final SearchResult expected, final SearchResult actual)
  {
    final SearchResult newResult = new SearchResult();
    for (LdapEntry e : actual.getEntries()) {
      newResult.addEntry(new LdapEntry(e.getDn(), e.getAttributes()));
    }
    AssertJUnit.assertEquals(expected, newResult);
  }


  /**
   * Invokes {@link AssertJUnit#assertEquals(Object, Object)} after converting the actual entry to an LdapEntry.
   *
   * @param  expected  value
   * @param  actual  value
   */
  public static void assertEquals(final LdapEntry expected, final LdapEntry actual)
  {
    final LdapEntry newEntry = new LdapEntry(actual.getDn(), actual.getAttributes());
    AssertJUnit.assertEquals(expected, newEntry);
  }


  /**
   * Returns the number of open connections to the supplied host. Uses 'netstat -al' to uncover open sockets.
   *
   * @param  host  host to look for.
   *
   * @return  number of open connections.
   *
   * @throws  IOException  if the process cannot be run
   */
  public static int countOpenConnections(final String host)
    throws IOException
  {
    final String[] cmd = new String[] {"netstat", "-al"};
    final Process p = new ProcessBuilder(cmd).start();
    final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    final List<String> openConns = new ArrayList<>();
    while ((line = br.readLine()) != null) {
      if (line.matches("(.*)ESTABLISHED(.*)")) {
        final String s = line.split("\\s+")[NETSTAT_HOST_INDEX];
        openConns.add(s);
      }
    }

    int count = 0;
    for (String o : openConns) {
      if (o.contains(host)) {
        count++;
      }
    }
    return count;
  }


  /**
   * Returns a string representation of the supplied byte array in hex format.
   *
   * @param  bytes  to create hex string with
   *
   * @return  hex string
   */
  public static String bytesToString(final byte[] bytes)
  {
    final StringBuilder sb = new StringBuilder(bytes.length * 2);
    // CheckStyle:MagicNumber OFF
    for (byte b : bytes) {
      final int v = b & 0xff;
      if (v < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(v)).append(":");
    }
    // CheckStyle:MagicNumber ON
    return sb.toString().toUpperCase();
  }
}
