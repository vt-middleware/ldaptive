/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.AbstractAssert;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.NoOpDnResolver;
import org.ldaptive.io.LdifReader;
import org.ldaptive.props.AuthenticatorPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.sasl.SaslConfig;
import org.ldaptive.transcode.ValueTranscoder;

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
   * @return  connection factory
   */
  public static ConnectionFactory createGssApiQopAuthConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi-qop-auth.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createGssApiQopAuthIntConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi-qop-auth-int.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createGssApiQopAuthIntLdapsConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi-qop-auth-int-ssl.properties"))
      .build();
  }


  /**
   * @return  connection factory
   */
  public static ConnectionFactory createGssApiUseConfigConnectionFactory()
  {
    return DefaultConnectionFactory.builder()
      .config(readConnectionConfig("classpath:/org/ldaptive/ldap.gssapi-use-config.properties"))
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
   * @param  initializer  to read properties from
   *
   * @return  new bind connection initializer
   */
  public static BindConnectionInitializer copyBindConnectionInitializer(final BindConnectionInitializer initializer)
  {
    final BindConnectionInitializer copy = new BindConnectionInitializer();
    copy.setBindDn(initializer.getBindDn());
    copy.setBindCredential(initializer.getBindCredential());
    copy.setBindSaslConfig(
      initializer.getBindSaslConfig() != null ? SaslConfig.copy(initializer.getBindSaslConfig()) : null);
    copy.setBindControls(initializer.getBindControls());
    return copy;
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
        result.append(line).append(System.lineSeparator());
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
   * Converts an ldif to a ldap result.
   *
   * @param  ldif  to convert.
   *
   * @return  ldap result.
   *
   * @throws  Exception  if ldif cannot be read
   */
  public static LdapEntry convertLdifToEntry(final String ldif)
    throws Exception
  {
    return convertLdifToResult(ldif).getEntry();
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
    return LdapUtils.toUpperCase(sb.toString());
  }


  /** Assert for testing ldap entries. */
  public static final class LdapEntryAssert extends AbstractAssert<LdapEntryAssert, LdapEntry>
  {


    public LdapEntryAssert(final LdapEntry actual)
    {
      super(actual, LdapEntryAssert.class);
    }


    public static LdapEntryAssert assertThat(final LdapEntry actual)
    {
      return new LdapEntryAssert(actual);
    }


    public LdapEntryAssert isSame(final LdapEntry expected, final String... caseIgnoreAttrs)
    {
      isNotNull();
      final LdapEntry newActual = lowerCaseEntry(actual, caseIgnoreAttrs);
      final LdapEntry newExpected = lowerCaseEntry(expected, caseIgnoreAttrs);
      if (!Objects.equals(newActual, newExpected)) {
        failWithMessage("Expected entry to be [%s] but was [%s]", newExpected, newActual);
      }
      return this;
    }


    public static LdapEntry lowerCaseEntry(final LdapEntry entry, final String... caseIgnoreAttrs)
    {
      final LdapEntry newEntry = LdapEntry.builder().dn(entry.getDn()).build();
      if (caseIgnoreAttrs != null && caseIgnoreAttrs.length > 0) {
        for (LdapAttribute la : entry.getAttributes()) {
          boolean setAttr = false;
          for (String name : caseIgnoreAttrs) {
            if (name.equalsIgnoreCase(la.getName())) {
              newEntry.addAttributes(LowerCaseValueTranscoder.lowerCase(la));
              setAttr = true;
              break;
            }
          }
          if (!setAttr) {
            newEntry.addAttributes(la);
          }
        }
      } else {
        newEntry.addAttributes(entry.getAttributes());
      }
      return newEntry;
    }
  }


  /** Assert for testing search responses. */
  public static final class SearchResponseAssert extends AbstractAssert<SearchResponseAssert, SearchResponse>
  {


    public SearchResponseAssert(final SearchResponse actual)
    {
      super(actual, SearchResponseAssert.class);
    }


    public static SearchResponseAssert assertThat(final SearchResponse actual)
    {
      return new SearchResponseAssert(actual);
    }


    public SearchResponseAssert isSame(final SearchResponse expected, final String... caseIgnoreAttrs)
    {
      isNotNull();
      final SearchResponse newActual = new SearchResponse();
      actual.getEntries().forEach(e -> newActual.addEntries(LdapEntryAssert.lowerCaseEntry(e, caseIgnoreAttrs)));
      final SearchResponse newExpected = new SearchResponse();
      expected.getEntries().forEach(e -> newExpected.addEntries(LdapEntryAssert.lowerCaseEntry(e, caseIgnoreAttrs)));
      if (!Objects.equals(newActual, newExpected)) {
        failWithMessage("Expected response to be [%s] but was [%s]", newExpected, newActual);
      }
      return this;
    }
  }


  /** Decodes and encodes a string by invoking {@link LdapUtils#toLowerCase(String)}. */
  private static final class LowerCaseValueTranscoder implements ValueTranscoder<String>
  {

    /** for lower casing values. */
    private static final LowerCaseValueTranscoder TRANSCODER = new LowerCaseValueTranscoder();


    @Override
    public String decodeStringValue(final String value)
    {
      return LdapUtils.toLowerCase(value);
    }


    @Override
    public String decodeBinaryValue(final byte[] value)
    {
      return LdapUtils.toLowerCase(new String(value, StandardCharsets.UTF_8));
    }


    @Override
    public String encodeStringValue(final String value)
    {
      return LdapUtils.toLowerCase(value);
    }


    @Override
    public byte[] encodeBinaryValue(final String value)
    {
      return LdapUtils.toLowerCase(value).getBytes(StandardCharsets.UTF_8);
    }


    @Override
    public Class<String> getType()
    {
      return String.class;
    }


    /**
     * Returns a new ldap attribute whose values have been lower cased.
     *
     * @param  la  attribute to copy values from
     *
     * @return  ldap attribute with lower cased values
     *
     * @throws  IllegalArgumentException  if a binary attribute is supplied
     */
    public static LdapAttribute lowerCase(final LdapAttribute la)
    {
      try {
        final LdapAttribute lowerCase = new LdapAttribute();
        lowerCase.setName(la.getName());
        lowerCase.addStringValues(la.getValues(TRANSCODER.decoder()));
        return lowerCase;
      } catch (UnsupportedOperationException e) {
        throw new IllegalArgumentException("Error lower casing attribute " + la, e);
      }
    }
  }
}
