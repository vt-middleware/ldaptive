/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads X.509 certificate credentials from a classpath, filepath, or stream
 * resource. Supported certificate formats include: PEM, DER, and PKCS7.
 *
 * @author  Middleware Services
 */
public class X509CertificatesCredentialReader
  extends AbstractCredentialReader<X509Certificate[]>
{


  @Override
  public X509Certificate[] read(final InputStream is, final String... params)
    throws IOException, GeneralSecurityException
  {
    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    final List<X509Certificate> certList = new ArrayList<>();
    final InputStream bufIs = getBufferedInputStream(is);
    while (bufIs.available() > 0) {
      final X509Certificate cert = (X509Certificate) cf.generateCertificate(
        bufIs);
      if (cert != null) {
        certList.add(cert);
      }
    }
    return certList.toArray(new X509Certificate[certList.size()]);
  }
}
