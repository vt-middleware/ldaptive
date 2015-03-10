/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
// CheckStyle:IllegalImport OFF
import sun.security.util.HostnameChecker;
// CheckStyle:IllegalImport ON

/**
 * A {@link HostnameVerifier} that delegates to the internal Sun implementation at sun.security.util.HostnameChecker.
 * This is the implementation used by JNDI with StartTLS.
 *
 * @author  Middleware Services
 */
public class SunTLSHostnameVerifier implements HostnameVerifier, CertificateHostnameVerifier
{


  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    boolean b;
    try {
      b = verify(hostname, (X509Certificate) session.getPeerCertificates()[0]);
    } catch (SSLPeerUnverifiedException e) {
      b = false;
    }
    return b;
  }


  /**
   * Expose convenience method for testing.
   *
   * @param  hostname  to verify
   * @param  cert  to verify hostname against
   *
   * @return  whether the certificate is allowed
   */
  @Override
  public boolean verify(final String hostname, final X509Certificate cert)
  {
    boolean b;
    final HostnameChecker checker = HostnameChecker.getInstance(HostnameChecker.TYPE_LDAP);
    try {
      checker.match(hostname, cert);
      b = true;
    } catch (CertificateException e) {
      b = false;
    }
    return b;
  }
}
