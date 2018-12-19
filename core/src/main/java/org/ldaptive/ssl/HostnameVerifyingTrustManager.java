/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trust manager that delegates to {@link CertificateHostnameVerifier}. Any name that verifies passes this trust manager
 * check.
 *
 * @author  Middleware Services
 */
public class HostnameVerifyingTrustManager extends X509ExtendedTrustManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

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
    if (names != null && names.length == 0) {
      hostnames = null;
    } else {
      hostnames = names;
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    if (hostnames != null) {
      checkCertificateTrusted(chain[0], hostnames);
    } else {
      checkCertificateTrusted(chain[0], ((SSLSocket) socket).getSession().getPeerHost());
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    if (hostnames != null) {
      checkCertificateTrusted(chain[0], hostnames);
    } else {
      checkCertificateTrusted(chain[0], engine.getSession().getPeerHost());
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    checkCertificateTrusted(chain[0], hostnames);
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    if (hostnames != null) {
      checkCertificateTrusted(chain[0], hostnames);
    } else {
      checkCertificateTrusted(chain[0], ((SSLSocket) socket).getSession().getPeerHost());
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    if (hostnames != null) {
      checkCertificateTrusted(chain[0], hostnames);
    } else {
      checkCertificateTrusted(chain[0], engine.getSession().getPeerHost());
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    checkCertificateTrusted(chain[0], hostnames);
  }


  /**
   * Verifies the supplied certificate using the hostname verifier with each hostname.
   *
   * @param  cert  to verify
   * @param  names  to match against cert
   *
   * @throws  CertificateException  if none of the hostnames verify
   */
  private void checkCertificateTrusted(final X509Certificate cert, final String... names)
    throws CertificateException
  {
    for (String name : names) {
      if (hostnameVerifier.verify(name, cert)) {
        logger.debug(
          "checkCertificateTrusted for {} succeeded against {}",
          hostnameVerifier,
          cert != null ? cert.getSubjectX500Principal() : null);
        return;
      }
    }
    throw new CertificateException(
      String.format(
        "Hostname '%s' does not match the hostname in the server's " +
        "certificate '%s'",
        Arrays.toString(names),
        cert != null ? cert.getSubjectX500Principal() : null));
  }


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::hostnameVerifier=%s, hostnames=%s]",
        getClass().getName(),
        hashCode(),
        hostnameVerifier,
        Arrays.toString(hostnames));
  }
}
