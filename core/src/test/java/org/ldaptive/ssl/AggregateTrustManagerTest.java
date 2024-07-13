/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.ldaptive.LdapUtils;
import org.ldaptive.ssl.AggregateTrustManager.Strategy;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link AggregateTrustManager}.
 * Generate key with: openssl ecparam -out test.key -name prime256v1 -genkey
 * Generate cert with: openssl req -new -x509 -sha256 -days 3650 -key test.key -out test.crt \ -subj
 * "/CN=a.foo.com/DC=ldaptive/DC=org" -config openssl.cnf \ -extensions my_ext
 *
 * @author  Middleware Services
 */
public class AggregateTrustManagerTest
{

  /** Certificate with CN=CA ONE. */
  private static final String CA_ONE_CERT =
    "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" +
    "Q2FkIN5Tm1ZoLdp9OiEoeFSyoWcYUQ==";

  /** Certificate with CN=CA TWO. */
  private static final String CA_TWO_CERT =
    "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" +
    "7ee5QajpLqXHGO1gRZGi7omFLq4E6Q==";

  /** Certificate with CN=a.foo.com signed by CA One. */
  private static final String A_FOO_COM_CERT =
    "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";

  /** Certificate with CN=a.bar.com signed by CA Two. */
  private static final String A_BAR_COM_CERT =
    "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";

  /** Certificate with CN=foo-client signed by CA One. */
  private static final String FOO_CLIENT_CERT =
    "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" +
    "SqGdsVbbFgd4grsy7JOJ";

  /** Certificate with CN=bar-client signed by CA Two. */
  private static final String BAR_CLIENT_CERT =
    "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" +
    "sWS5EylU66FlGFXmI8xH";

  /** a.foo.com cert chain. */
  private static final X509Certificate[] A_FOO_COM_CHAIN;

  /** a.bar.com cert chain. */
  private static final X509Certificate[] A_BAR_COM_CHAIN;

  /** foo-client cert chain. */
  private static final X509Certificate[] FOO_CLIENT_CHAIN;

  /** bar-client cert chain. */
  private static final X509Certificate[] BAR_CLIENT_CHAIN;

  /** ca one trust manager. */
  private static final X509TrustManager[] CA_ONE_TRUST_MANAGER;

  /** ca two trust manager. */
  private static final X509TrustManager[] CA_TWO_TRUST_MANAGER;
  static {
    try {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      // Server certificates
      final X509Certificate aFooComCert = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(A_FOO_COM_CERT)));
      final X509Certificate aBarComCert = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(A_BAR_COM_CERT)));
      // Client certificates
      final X509Certificate fooClientCert = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(FOO_CLIENT_CERT)));
      final X509Certificate barClientCert = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(BAR_CLIENT_CERT)));
      // CA certificates
      final X509Certificate caOne = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(CA_ONE_CERT)));
      final X509Certificate caTwo = (X509Certificate) cf.generateCertificate(
        new ByteArrayInputStream(LdapUtils.base64Decode(CA_TWO_CERT)));

      A_FOO_COM_CHAIN = new X509Certificate[] {aFooComCert, caOne};
      A_BAR_COM_CHAIN = new X509Certificate[] {aBarComCert, caTwo};
      FOO_CLIENT_CHAIN = new X509Certificate[] {fooClientCert, caOne};
      BAR_CLIENT_CHAIN = new X509Certificate[] {barClientCert, caOne};
      CA_ONE_TRUST_MANAGER = createTrustManagers(caOne);
      CA_TWO_TRUST_MANAGER = createTrustManagers(caTwo);
    } catch (Exception e) {
      throw new IllegalStateException("Could not initialize test certificate data", e);
    }
  }

  /**
   * Certificate test data.
   *
   * @return  cert test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "server-certs")
  public Object[][] createServerCerts()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, A_FOO_COM_CHAIN, true, },
        new Object[] {Strategy.ANY, CA_ONE_TRUST_MANAGER, A_FOO_COM_CHAIN, true, },
        new Object[] {Strategy.ALL, CA_TWO_TRUST_MANAGER, A_BAR_COM_CHAIN, true, },
        new Object[] {Strategy.ANY, CA_TWO_TRUST_MANAGER, A_BAR_COM_CHAIN, true, },
        new Object[] {Strategy.ALL, CA_TWO_TRUST_MANAGER, A_FOO_COM_CHAIN, false, },
        new Object[] {Strategy.ANY, CA_TWO_TRUST_MANAGER, A_FOO_COM_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, A_BAR_COM_CHAIN, false, },
        new Object[] {Strategy.ANY, CA_ONE_TRUST_MANAGER, A_BAR_COM_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, FOO_CLIENT_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, BAR_CLIENT_CHAIN, false, },
        new Object[] {
          Strategy.ALL,
          LdapUtils.concatArrays(CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          A_FOO_COM_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ALL,
          LdapUtils.concatArrays(
            CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()}),
          A_FOO_COM_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ANY,
          LdapUtils.concatArrays(CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          A_FOO_COM_CHAIN,
          true,
        },
        new Object[] {
          Strategy.ANY,
          new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()},
          A_FOO_COM_CHAIN,
          true,
        },
        new Object[] {
          Strategy.ANY,
          LdapUtils.concatArrays(CA_TWO_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          A_FOO_COM_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ANY,
          new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()},
          A_FOO_COM_CHAIN,
          true,
        },
      };
  }


  /**
   * Certificate test data.
   *
   * @return  cert test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "client-certs")
  public Object[][] createClientCerts()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, FOO_CLIENT_CHAIN, true, },
        new Object[] {Strategy.ANY, CA_ONE_TRUST_MANAGER, FOO_CLIENT_CHAIN, true, },
        new Object[] {Strategy.ALL, CA_TWO_TRUST_MANAGER, BAR_CLIENT_CHAIN, true, },
        new Object[] {Strategy.ANY, CA_TWO_TRUST_MANAGER, BAR_CLIENT_CHAIN, true, },
        new Object[] {Strategy.ALL, CA_TWO_TRUST_MANAGER, FOO_CLIENT_CHAIN, false, },
        new Object[] {Strategy.ANY, CA_TWO_TRUST_MANAGER, FOO_CLIENT_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, BAR_CLIENT_CHAIN, false, },
        new Object[] {Strategy.ANY, CA_ONE_TRUST_MANAGER, BAR_CLIENT_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, A_FOO_COM_CHAIN, false, },
        new Object[] {Strategy.ALL, CA_ONE_TRUST_MANAGER, A_BAR_COM_CHAIN, false, },
        new Object[] {
          Strategy.ALL,
          LdapUtils.concatArrays(CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          FOO_CLIENT_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ALL,
          LdapUtils.concatArrays(
            CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()}),
          FOO_CLIENT_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ANY,
          LdapUtils.concatArrays(CA_ONE_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          FOO_CLIENT_CHAIN,
          true,
        },
        new Object[] {
          Strategy.ANY,
          new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()},
          FOO_CLIENT_CHAIN,
          true,
        },
        new Object[] {
          Strategy.ANY,
          LdapUtils.concatArrays(CA_TWO_TRUST_MANAGER, new X509TrustManager[] {new DefaultTrustManager()}),
          FOO_CLIENT_CHAIN,
          false,
        },
        new Object[] {
          Strategy.ANY,
          new X509TrustManager[] {new DefaultTrustManager(), new AllowAnyTrustManager()},
          FOO_CLIENT_CHAIN,
          true,
        },
      };
  }


  @Test(groups = "ssl", dataProvider = "server-certs")
  public void checkServerTrusted(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkServerTrusted(chain, "RSA");
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "server-certs")
  public void checkServerTrustedSSLEngine(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkServerTrusted(chain, "RSA", new MockSSLEngine(new DefaultSSLContextInitializer()));
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "server-certs")
  public void checkServerTrustedSocket(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkServerTrusted(chain, "RSA", new MockSSLSocket(new DefaultSSLContextInitializer()));
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "client-certs")
  public void checkClientTrusted(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkClientTrusted(chain, "RSA");
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "client-certs")
  public void checkClientTrustedSSLEngine(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkClientTrusted(chain, "RSA", new MockSSLEngine(new DefaultSSLContextInitializer()));
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "client-certs")
  public void checkClientTrustedSocket(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    try {
      trustManager.checkClientTrusted(chain, "RSA", new MockSSLSocket(new DefaultSSLContextInitializer()));
      if (!success) {
        fail("Should have thrown exception for %s", Arrays.toString(trustManagers));
      }
    } catch (Exception e) {
      if (success) {
        fail("Should not have thrown exception for %s", Arrays.toString(trustManagers), e);
      }
    }
  }


  @Test(groups = "ssl", dataProvider = "server-certs")
  public void getAcceptedIssuersServer(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    final X509Certificate[] issuers = trustManager.getAcceptedIssuers();
    assertThat(issuers).isNotNull();
  }


  @Test(groups = "ssl", dataProvider = "client-certs")
  public void getAcceptedIssuersClient(
    final Strategy strategy,
    final X509TrustManager[] trustManagers,
    final X509Certificate[] chain,
    final boolean success)
  {
    final AggregateTrustManager trustManager = new AggregateTrustManager(strategy, trustManagers);
    final X509Certificate[] issuers = trustManager.getAcceptedIssuers();
    assertThat(issuers).isNotNull();
  }


  /**
   * Creates trust managers for the supplied certificates.
   *
   * @param  certs  to trust
   * @return  trust managers
   *
   * @throws  GeneralSecurityException  if a keystore cannot be created from the certs
   */
  private static X509TrustManager[] createTrustManagers(final X509Certificate... certs)
    throws GeneralSecurityException
  {
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    final KeyStore ks = KeyStoreUtils.newInstance("JKS");
    for (int i = 0; i < certs.length; i++) {
      KeyStoreUtils.setCertificateEntry(String.valueOf(i), ks, certs[i]);
    }
    tmf.init(ks);
    return Stream.of(tmf.getTrustManagers())
      .map(X509TrustManager.class::cast)
      .toArray(X509TrustManager[]::new);
  }
}
