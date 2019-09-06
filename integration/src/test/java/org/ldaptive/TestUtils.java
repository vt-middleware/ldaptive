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
    final ConnectionConfigPropertySource ccSource;
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
    final AuthenticatorPropertySource aSource;
    if (path != null) {
      aSource = new AuthenticatorPropertySource(a, path);
    } else {
      aSource = new AuthenticatorPropertySource(a);
    }
    aSource.initialize();
    return a;
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createSetupConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.setup.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig(null))
      .build();
  }


  /**
   * @return  connection factory
   *
   * @throws  LdapException  if the factory cannot be initialized
   */
  public static SingleConnectionFactory createSingleConnectionFactory()
    throws LdapException
  {
    final SingleConnectionFactory cf =  SingleConnectionFactory.builder()
      .config(readConnectionConfig(null))
      .build();
    cf.initialize();
    return cf;
  }


  /**
   * @return  connection factory
   *
   * @throws  LdapException  if the factory cannot be initialized
   */
  public static PooledConnectionFactory createPooledConnectionFactory()
    throws LdapException
  {
    final PooledConnectionFactory cf =  PooledConnectionFactory.builder()
      .config(readConnectionConfig(null))
      .build();
    cf.initialize();
    return cf;
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createSaslExternalConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.external.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createDigestMd5ConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.digest-md5.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createCramMd5ConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.cram-md5.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createGssApiConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi.properties"))
      .build();
  }


  /**
   * @return  authenticator
   */
  public static Authenticator createSSLAuthenticator()
  {
    return readAuthenticator("classpath:/org/ldaptive/ldap.ssl.properties");
  }


  /**
   * @return  authenticator
   */
  public static Authenticator createSSLDnAuthenticator()
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.ssl.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * @return  authenticator
   */
  public static Authenticator createTLSAuthenticator()
  {
    return readAuthenticator("classpath:/org/ldaptive/ldap.tls.properties");
  }


  /**
   * @return  authenticator
   */
  public static Authenticator createTLSDnAuthenticator()
  {
    final Authenticator auth = readAuthenticator("classpath:/org/ldaptive/ldap.tls.properties");
    auth.setDnResolver(new NoOpDnResolver());
    return auth;
  }


  /**
   * Reads a file on the classpath into a reader.
   *
   * @param  filename  to open.
   *
   * @return  reader.
   */
  public static BufferedReader readFile(final String filename)
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
  public static SearchResponse convertLdifToResult(final String ldif)
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
    final LdapEntry le = LdapEntry.builder().dn(dn).build();
    for (String s : attrs.split("\\|")) {
      final String[] nameValuePairs = s.trim().split("=", 2);
      if (le.getAttribute(nameValuePairs[0]) != null) {
        le.getAttribute(nameValuePairs[0]).addStringValues(nameValuePairs[1]);
      } else {
        le.addAttributes(new LdapAttribute(nameValuePairs[0], nameValuePairs[1]));
      }
    }
    return le;
  }


  /**
   * Invokes {@link AssertJUnit#assertEquals(Object, Object)} after removing the controls and messageId from the actual
   * response entries.
   *
   * @param  expected  value
   * @param  actual  value
   */
  public static void assertEquals(final SearchResponse expected, final SearchResponse actual)
  {
    final SearchResponse newResult = new SearchResponse();
    for (LdapEntry e : actual.getEntries()) {
      AssertJUnit.assertNotNull(e);
      newResult.addEntries(LdapEntry.builder().dn(e.getDn()).attributes(e.getAttributes()).build());
    }
    AssertJUnit.assertEquals(expected, newResult);
  }


  /**
   * Invokes {@link AssertJUnit#assertEquals(Object, Object)} after removing the controls and messageId from the actual
   * entry.
   *
   * @param  expected  value
   * @param  actual  value
   */
  public static void assertEquals(final LdapEntry expected, final LdapEntry actual)
  {
    AssertJUnit.assertNotNull(actual);
    final LdapEntry newEntry = LdapEntry.builder().dn(actual.getDn()).attributes(actual.getAttributes()).build();
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
