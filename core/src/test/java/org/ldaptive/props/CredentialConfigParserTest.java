/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import org.ldaptive.ssl.KeyStoreCredentialConfig;
import org.ldaptive.ssl.X509CredentialConfig;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CredentialConfigParser}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class CredentialConfigParserTest
{


  /**
   * Property test data.
   *
   * @return  configuration properties
   */
  @DataProvider(name = "properties")
  public Object[][] createProperties()
  {
    final String p1 = "{trustCertificates=classpath:/ldaptive.trust.crt}";
    final X509CredentialConfig o1 = new X509CredentialConfig();
    o1.setTrustCertificates("classpath:/ldaptive.trust.crt");

    final String p2 = "{{trustCertificates=classpath:/ldaptive.trust.crt}}";
    final X509CredentialConfig o2 = new X509CredentialConfig();
    o2.setTrustCertificates("classpath:/ldaptive.trust.crt");

    final String p3 = "{trustCertificates=classpath:/ldaptive.trust.crt}" +
      "{authenticationCertificate=classpath:/ldaptive.crt}";
    final X509CredentialConfig o3 = new X509CredentialConfig();
    o3.setTrustCertificates("classpath:/ldaptive.trust.crt");
    o3.setAuthenticationCertificate("classpath:/ldaptive.crt");

    final String p4 = "{{trustCertificates=classpath:/ldaptive.trust.crt}" +
      "{authenticationCertificate=classpath:/ldaptive.crt}}";
    final X509CredentialConfig o4 = new X509CredentialConfig();
    o4.setTrustCertificates("classpath:/ldaptive.trust.crt");
    o4.setAuthenticationCertificate("classpath:/ldaptive.crt");

    final String p5 = "org.ldaptive.ssl.X509CredentialConfig" +
      "{trustCertificates=file:ldaptive.trust.crt}";
    final X509CredentialConfig o5 = new X509CredentialConfig();
    o5.setTrustCertificates("file:ldaptive.trust.crt");

    final String p6 = "org.ldaptive.ssl.X509CredentialConfig" +
      "{{trustCertificates=file:ldaptive.trust.crt}}";
    final X509CredentialConfig o6 = new X509CredentialConfig();
    o6.setTrustCertificates("file:ldaptive.trust.crt");

    final String p7 = "org.ldaptive.ssl.KeyStoreCredentialConfig" +
      "{{trustStore=classpath:/ldaptive.truststore}{trustStoreType=BKS}}";
    final KeyStoreCredentialConfig o7 = new KeyStoreCredentialConfig();
    o7.setTrustStore("classpath:/ldaptive.truststore");
    o7.setTrustStoreType("BKS");

    final String p8 = "org.ldaptive.ssl.KeyStoreCredentialConfig" +
      "{{trustStore=classpath:/ldaptive.truststore} {trustStoreType=BKS}}";
    final KeyStoreCredentialConfig o8 = new KeyStoreCredentialConfig();
    o8.setTrustStore("classpath:/ldaptive.truststore");
    o8.setTrustStoreType("BKS");

    final String p9 = "org.ldaptive.ssl.KeyStoreCredentialConfig" +
      "{{trustStore=classpath:/ldaptive.truststore}" +
      "{trustStoreType=BKS}" +
      "{trustStoreAliases=alias1}}";
    final KeyStoreCredentialConfig o9 = new KeyStoreCredentialConfig();
    o9.setTrustStore("classpath:/ldaptive.truststore");
    o9.setTrustStoreType("BKS");
    o9.setTrustStoreAliases("alias1");

    final String p10 = "org.ldaptive.ssl.KeyStoreCredentialConfig" +
      "{{trustStore=classpath:/ldaptive.truststore}" +
      "{trustStoreType=BKS}" +
      "{trustStoreAliases=alias1,alias2}}";
    final KeyStoreCredentialConfig o10 = new KeyStoreCredentialConfig();
    o10.setTrustStore("classpath:/ldaptive.truststore");
    o10.setTrustStoreType("BKS");
    o10.setTrustStoreAliases("alias1", "alias2");

    final String p11 = "org.ldaptive.ssl.KeyStoreCredentialConfig" +
      "{{trustStore=file:/path/to/my/cacerts}" +
      "{trustStorePassword=changeit}" +
      "{trustStoreAliases=custom-1,custom-2}" +
      "{keyStore=file:/path/to/my/ldaptive.keystore}" +
      "{keyStorePassword=changeit}" +
      "{keyStoreAliases=alias1,alias2}}";
    final KeyStoreCredentialConfig o11 = new KeyStoreCredentialConfig();
    o11.setTrustStore("file:/path/to/my/cacerts");
    o11.setTrustStorePassword("changeit");
    o11.setTrustStoreAliases("custom-1", "custom-2");
    o11.setKeyStore("file:/path/to/my/ldaptive.keystore");
    o11.setKeyStorePassword("changeit");
    o11.setKeyStoreAliases("alias1", "alias2");

    return
      new Object[][] {
        new Object[] {p1, o1, },
        new Object[] {p2, o2, },
        new Object[] {p3, o3, },
        new Object[] {p4, o4, },
        new Object[] {p5, o5, },
        new Object[] {p6, o6, },
        new Object[] {p7, o7, },
        new Object[] {p8, o8, },
        new Object[] {p9, o9, },
        new Object[] {p10, o10, },
        new Object[] {p11, o11, },
      };
  }


  /**
   * @param  property  to test
   * @param  initialized  object to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"props"},
    dataProvider = "properties"
  )
  public void initializeType(final String property, final Object initialized)
    throws Exception
  {
    Assert.assertTrue(CredentialConfigParser.isCredentialConfig(property));

    final CredentialConfigParser parser = new CredentialConfigParser(property);
    Assert.assertEquals(initialized, parser.initializeType());
  }
}
