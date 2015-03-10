/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import org.ldaptive.LdapUtils;

/**
 * Utility class for creating credential configs when the underlying credential is already available for use.
 *
 * @author  Middleware Services
 */
public final class CredentialConfigFactory
{


  /** Default constructor. */
  private CredentialConfigFactory() {}


  /**
   * Creates a KeyStoreCredentialConfig from the supplied truststore.
   *
   * @param  trustStore  to create credential config from
   *
   * @return  credential config
   */
  public static CredentialConfig createKeyStoreCredentialConfig(final KeyStore trustStore)
  {
    return createKeyStoreCredentialConfig(trustStore, null, null);
  }


  /**
   * Creates a KeyStoreCredentialConfig from the supplied keystore and password.
   *
   * @param  keyStore  to create credential config from
   * @param  keyStorePassword  to unlock the keystore
   *
   * @return  credential config
   */
  public static CredentialConfig createKeyStoreCredentialConfig(final KeyStore keyStore, final String keyStorePassword)
  {
    return createKeyStoreCredentialConfig(null, keyStore, keyStorePassword);
  }


  /**
   * Creates a KeyStoreCredentialConfig from the supplied truststore, keystore and password.
   *
   * @param  trustStore  to create credential config from
   * @param  keyStore  to create credential config from
   * @param  keyStorePassword  to unlock the keystore
   *
   * @return  credential config
   */
  public static CredentialConfig createKeyStoreCredentialConfig(
    final KeyStore trustStore,
    final KeyStore keyStore,
    final String keyStorePassword)
  {
    return
      new CredentialConfig() {
      @Override
      public SSLContextInitializer createSSLContextInitializer()
        throws GeneralSecurityException
      {
        final KeyStoreSSLContextInitializer sslInit = new KeyStoreSSLContextInitializer();
        if (trustStore != null) {
          sslInit.setTrustKeystore(trustStore);
        }
        if (keyStore != null) {
          sslInit.setAuthenticationKeystore(keyStore);
          sslInit.setAuthenticationPassword(keyStorePassword != null ? keyStorePassword.toCharArray() : null);
        }
        return sslInit;
      }
    };
  }


  /**
   * Creates a X509CredentialConfig from the supplied trust certificates.
   *
   * @param  trustCertificates  to create credential config from
   *
   * @return  credential config
   */
  public static CredentialConfig createX509CredentialConfig(final X509Certificate[] trustCertificates)
  {
    return createX509CredentialConfig(trustCertificates, null, null);
  }


  /**
   * Creates a X509CredentialConfig from the supplied authentication certificate and private key.
   *
   * @param  authenticationCertificate  to create credential config from
   * @param  authenticationKey  that belongs to the certificate
   *
   * @return  credential config
   */
  public static CredentialConfig createX509CredentialConfig(
    final X509Certificate authenticationCertificate,
    final PrivateKey authenticationKey)
  {
    return createX509CredentialConfig(null, authenticationCertificate, authenticationKey);
  }


  /**
   * Creates a X509CredentialConfig from the supplied trust certificates, authentication certificate and private key.
   *
   * @param  trustCertificates  to create credential config from
   * @param  authenticationCertificate  to create credential config from
   * @param  authenticationKey  that belongs to the certificate
   *
   * @return  credential config
   */
  public static CredentialConfig createX509CredentialConfig(
    final X509Certificate[] trustCertificates,
    final X509Certificate authenticationCertificate,
    final PrivateKey authenticationKey)
  {
    return
      new CredentialConfig() {
      @Override
      public SSLContextInitializer createSSLContextInitializer()
        throws GeneralSecurityException
      {
        final X509SSLContextInitializer sslInit = new X509SSLContextInitializer();
        if (trustCertificates != null) {
          sslInit.setTrustCertificates(trustCertificates);
        }
        if (authenticationCertificate != null) {
          sslInit.setAuthenticationCertificate(authenticationCertificate);
        }
        if (authenticationKey != null) {
          sslInit.setAuthenticationKey(authenticationKey);
        }
        return sslInit;
      }
    };
  }


  /**
   * Creates a X509CredentialConfig from PEM encoded certificate(s).
   *
   * @param  trustCertificates  to create credential config from
   *
   * @return  credential config
   */
  public static CredentialConfig createX509CredentialConfig(final String trustCertificates)
  {
    return
      new CredentialConfig() {
      @Override
      public SSLContextInitializer createSSLContextInitializer()
        throws GeneralSecurityException
      {
        final X509SSLContextInitializer sslInit = new X509SSLContextInitializer();
        try {
          if (trustCertificates != null) {
            final X509CertificatesCredentialReader certsReader = new X509CertificatesCredentialReader();
            final InputStream trustCertStream = new ByteArrayInputStream(LdapUtils.utf8Encode(trustCertificates));
            sslInit.setTrustCertificates(certsReader.read(trustCertStream));
            trustCertStream.close();
          }
        } catch (IOException e) {
          throw new GeneralSecurityException(e);
        }
        return sslInit;
      }
    };
  }
}
