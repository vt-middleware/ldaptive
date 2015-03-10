/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.ldaptive.LdapUtils;

/**
 * Provides the properties necessary for creating an SSL context initializer with an X.509 credential reader.
 *
 * @author  Middleware Services
 */
public class X509CredentialConfig implements CredentialConfig
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 1009;

  /** Reads X.509 certificates credential. */
  private final X509CertificatesCredentialReader certsReader = new X509CertificatesCredentialReader();

  /** Reads X.509 certificate credential. */
  private final X509CertificateCredentialReader certReader = new X509CertificateCredentialReader();

  /** Reads private key credential. */
  private final PrivateKeyCredentialReader keyReader = new PrivateKeyCredentialReader();

  /** Name of the trust certificates to use for the SSL connection. */
  private String trustCertificates;

  /** Name of the authentication certificate to use for the SSL connection. */
  private String authenticationCertificate;

  /** Name of the key to use for the SSL connection. */
  private String authenticationKey;


  /**
   * Returns the name of the trust certificates to use.
   *
   * @return  trust certificates name
   */
  public String getTrustCertificates()
  {
    return trustCertificates;
  }


  /**
   * Sets the name of the trust certificates to use.
   *
   * @param  name  trust certificates name
   */
  public void setTrustCertificates(final String name)
  {
    trustCertificates = name;
  }


  /**
   * Returns the name of the authentication certificate to use.
   *
   * @return  authentication certificate name
   */
  public String getAuthenticationCertificate()
  {
    return authenticationCertificate;
  }


  /**
   * Sets the name of the authentication certificate to use.
   *
   * @param  name  authentication certificate name
   */
  public void setAuthenticationCertificate(final String name)
  {
    authenticationCertificate = name;
  }


  /**
   * Returns the name of the authentication key to use.
   *
   * @return  authentication key name
   */
  public String getAuthenticationKey()
  {
    return authenticationKey;
  }


  /**
   * Sets the name of the authentication key to use.
   *
   * @param  name  authentication key name
   */
  public void setAuthenticationKey(final String name)
  {
    authenticationKey = name;
  }


  @Override
  public SSLContextInitializer createSSLContextInitializer()
    throws GeneralSecurityException
  {
    final X509SSLContextInitializer sslInit = new X509SSLContextInitializer();
    try {
      if (trustCertificates != null) {
        sslInit.setTrustCertificates(certsReader.read(trustCertificates));
      }
      if (authenticationCertificate != null) {
        sslInit.setAuthenticationCertificate(certReader.read(authenticationCertificate));
      }
      if (authenticationKey != null) {
        sslInit.setAuthenticationKey(keyReader.read(authenticationKey));
      }
    } catch (IOException e) {
      throw new GeneralSecurityException(e);
    }
    return sslInit;
  }


  @Override
  public boolean equals(final Object o)
  {
    return LdapUtils.areEqual(this, o);
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, trustCertificates, authenticationCertificate, authenticationKey);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::trustCertificates=%s, authenticationCertificate=%s, " +
        "authenticationKey=%s]",
        getClass().getName(),
        hashCode(),
        trustCertificates,
        authenticationCertificate,
        authenticationKey);
  }
}
