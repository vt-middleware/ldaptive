/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.X509TrustManager;

/**
 * Trust manager that delegates to {@link CertificateHostnameVerifier}. Any name that verifies passes this trust manager
 * check.
 *
 * @author  Middleware Services
 */
public class HostnameVerifyingTrustManager implements X509TrustManager
{

  /** Hostnames to allow. */
  private final String[] hostnames;

  /** Hostname verifier to use for trust. */
  private final CertificateHostnameVerifier hostnameVerifier;


  /**
   * Creates a new hostname verifying trust manager.
   *
   * @param  verifier  that establishes trust
   * @param  names  to match against a certificate
   */
  public HostnameVerifyingTrustManager(final CertificateHostnameVerifier verifier, final String... names)
  {
    hostnameVerifier = verifier;
    hostnames = names;
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    checkCertificateTrusted(chain[0]);
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    checkCertificateTrusted(chain[0]);
  }


  /**
   * Verifies the supplied certificate using the hostname verifier with each hostname.
   *
   * @param  cert  to verify
   *
   * @throws  CertificateException  if none of the hostnames verify
   */
  private void checkCertificateTrusted(final X509Certificate cert)
    throws CertificateException
  {
    for (String name : hostnames) {
      if (hostnameVerifier.verify(name, cert)) {
        return;
      }
    }
    throw new CertificateException(
      String.format(
        "Hostname '%s' does not match the hostname in the server's " +
        "certificate",
        Arrays.toString(hostnames)));
  }


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
