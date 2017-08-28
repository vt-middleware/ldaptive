/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SSLContextInitializer}.
 *
 * @author  Middleware Services
 */
public class SSLContextInitializerTest
{

  /** Certificate with CN=a.foo.com. */
  private static final String A_FOO_COM_CERT =
    "MIIDrzCCApegAwIBAgIJAK+nL4I3GkjeMA0GCSqGSIb3DQEBBQUAMEMxEjAQBgNV" +
      "BAMTCWEuZm9vLmNvbTEYMBYGCgmSJomT8ixkARkWCGxkYXB0aXZlMRMwEQYKCZIm" +
      "iZPyLGQBGRYDb3JnMB4XDTEyMDExNzIxNDAxNVoXDTIyMDExNDIxNDAxNVowQzES" +
      "MBAGA1UEAxMJYS5mb28uY29tMRgwFgYKCZImiZPyLGQBGRYIbGRhcHRpdmUxEzAR" +
      "BgoJkiaJk/IsZAEZFgNvcmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB" +
      "AQDGRxBVvGZqHFWbYdbpOZaBf4H68b7zjiqbpXXq+mTfVOehIUeyL0624JsdmHLx" +
      "oMNC7K9hAaM88wxcyhBRLRfo4ar1DJspzcFUoz2kFD7ytWGS2zwvV+VWnoXpPNiw" +
      "9QuK6bdA/UYLIg/fk3TwshuoIT9VBJ4L3TRdOYYgH6WJBerQ2L5vMu91B9nBhNqR" +
      "4RG8VFqwgwW9IoXBXC8XTZS5jd0bVoEoeA+PWVENQ3my5ilP4VUqo9h/jPdb8dFW" +
      "3TNoaVHjOiTUOIpH+5cUmi0OkH2NzhTaWmCVoWuzFpvvB6PFHHxut2pDe8eGgc4x" +
      "NdEvZDizbfY6JEb/fkwZ+Im9AgMBAAGjgaUwgaIwHQYDVR0OBBYEFPUscUXspD8Z" +
      "LP3b6yybVhXhp5C2MHMGA1UdIwRsMGqAFPUscUXspD8ZLP3b6yybVhXhp5C2oUek" +
      "RTBDMRIwEAYDVQQDEwlhLmZvby5jb20xGDAWBgoJkiaJk/IsZAEZFghsZGFwdGl2" +
      "ZTETMBEGCgmSJomT8ixkARkWA29yZ4IJAK+nL4I3GkjeMAwGA1UdEwQFMAMBAf8w" +
      "DQYJKoZIhvcNAQEFBQADggEBALam5DdoM7cyOS2GbiA7QAfZTJkBcVr4Fef9aDWR" +
      "cG3kzbEbu1OXf3lkRW11H7gPLOgZGebSsxsv6YhKgAtz7py3lyH5QNkrN0OGI1ZA" +
      "eXf76eSR4T26pYjxln26xyZUW/dcddQ0nSj9Yl52oFCWj38DqGaxP6hIu3DHGlcE" +
      "PtpM2T4ZjWgrsqxL8N59zMb0Re9V4Xop7KmsLs3ThF3RWwmZdC1ba5LRPK6lKNF5" +
      "CnSl5YzFUMnpzFZtneUhAHeFxrF+RV4f3bHLNs+sWjlmJo0ukCCnOzoiyE4oOJiL" +
      "AhDym4nIfzng6fgYBeLT1Hp/bKHivQP4ef4wgre6r1ztnFA=";

  /** Certificate for testing. */
  private final X509Certificate testCert;


  /**
   * Default constructor.
   *
   * @throws  Exception  on test failure
   */
  public SSLContextInitializerTest()
    throws Exception
  {
    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    testCert = (X509Certificate) cf.generateCertificate(
      new ByteArrayInputStream(LdapUtils.base64Decode(A_FOO_COM_CERT)));
  }


  /**
   * SSLContextInitializer test data.
   *
   * @return  cert test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "initializers")
  public Object[][] createInitializers()
    throws Exception
  {
    // default ssl context initializer
    final DefaultSSLContextInitializer defaultWithTM = new DefaultSSLContextInitializer();
    defaultWithTM.setTrustManagers(new AllowAnyTrustManager());
    final DefaultSSLContextInitializer defaultWithHV = new DefaultSSLContextInitializer();
    defaultWithHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final DefaultSSLContextInitializer defaultWithTMHV = new DefaultSSLContextInitializer();
    defaultWithTMHV.setTrustManagers(new AllowAnyTrustManager());
    defaultWithTMHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final DefaultSSLContextInitializer defaultNoTrustWithTM = new DefaultSSLContextInitializer(false);
    defaultNoTrustWithTM.setTrustManagers(new AllowAnyTrustManager());
    final DefaultSSLContextInitializer defaultNoTrustWithHV = new DefaultSSLContextInitializer(false);
    defaultNoTrustWithHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final DefaultSSLContextInitializer defaultNoTrustWithTMHV = new DefaultSSLContextInitializer(false);
    defaultNoTrustWithTMHV.setTrustManagers(new AllowAnyTrustManager());
    defaultNoTrustWithTMHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));

    // x509 ssl context initializer
    final X509SSLContextInitializer x509 = new X509SSLContextInitializer();
    x509.setTrustCertificates(testCert);
    final X509SSLContextInitializer x509WithTM = new X509SSLContextInitializer();
    x509WithTM.setTrustCertificates(testCert);
    x509WithTM.setTrustManagers(new AllowAnyTrustManager());
    final X509SSLContextInitializer x509WithHV = new X509SSLContextInitializer();
    x509WithHV.setTrustCertificates(testCert);
    x509WithHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final X509SSLContextInitializer x509WithTMHV = new X509SSLContextInitializer();
    x509WithTMHV.setTrustCertificates(testCert);
    x509WithTMHV.setTrustManagers(new AllowAnyTrustManager());
    x509WithTMHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final X509SSLContextInitializer x509NoTrustWithTM = new X509SSLContextInitializer();
    x509NoTrustWithTM.setTrustManagers(new AllowAnyTrustManager());
    final X509SSLContextInitializer x509NoTrustWithHV = new X509SSLContextInitializer();
    x509NoTrustWithHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));
    final X509SSLContextInitializer x509NoTrustWithTMHV = new X509SSLContextInitializer();
    x509NoTrustWithTMHV.setTrustManagers(new AllowAnyTrustManager());
    x509NoTrustWithTMHV.setHostnameVerifierConfig(new HostnameVerifierConfig(new DefaultHostnameVerifier(), "test"));

    return
      new Object[][] {
        new Object[] {new DefaultSSLContextInitializer(), "sun.security.ssl.X509TrustManagerImpl", },
        new Object[] {
          defaultWithTM,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.AllowAnyTrustManager",
        },
        new Object[] {
          defaultWithHV,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {
          defaultWithTMHV,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.AllowAnyTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {new DefaultSSLContextInitializer(false), "", },
        new Object[] {
          defaultNoTrustWithTM,
          "org.ldaptive.ssl.AllowAnyTrustManager",
        },
        // default trust added
        new Object[] {
          defaultNoTrustWithHV,
          "org.ldaptive.ssl.DefaultTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {
          defaultNoTrustWithTMHV,
          "org.ldaptive.ssl.AllowAnyTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {
          x509,
          "sun.security.ssl.X509TrustManagerImpl",
        },
        new Object[] {
          x509WithTM,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.AllowAnyTrustManager",
        },
        new Object[] {
          x509WithHV,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {
          x509WithTMHV,
          "sun.security.ssl.X509TrustManagerImpl",
          "org.ldaptive.ssl.AllowAnyTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {new X509SSLContextInitializer(), "", },
        new Object[] {
          x509NoTrustWithTM,
          "org.ldaptive.ssl.AllowAnyTrustManager",
        },
        // default trust added
        new Object[] {
          x509NoTrustWithHV,
          "org.ldaptive.ssl.DefaultTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
        new Object[] {
          x509NoTrustWithTMHV,
          "org.ldaptive.ssl.AllowAnyTrustManager",
          "org.ldaptive.ssl.HostnameVerifyingTrustManager",
        },
      };
  }


  /**
   * @param  initializer  to get trust managers from
   * @param  clazz  types to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"ssl"}, dataProvider = "initializers")
  public void getTrustManagers(final SSLContextInitializer initializer, final String... clazz)
    throws Exception
  {
    final TrustManager[] tm = initializer.getTrustManagers();
    if (tm == null) {
      Assert.assertEquals(1, clazz.length);
      Assert.assertEquals("", clazz[0]);
    } else {
      Assert.assertEquals(tm.length, 1);
      Assert.assertTrue(tm[0] instanceof AggregateTrustManager);
      final AggregateTrustManager aggregate = (AggregateTrustManager) tm[0];
      Assert.assertEquals(aggregate.getTrustManagers().length, clazz.length);
      for (int i = 0; i < aggregate.getTrustManagers().length; i++) {
        Assert.assertEquals(aggregate.getTrustManagers()[i].getClass().getName(), clazz[i]);
      }
    }
  }
}
