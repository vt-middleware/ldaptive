/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Loads an X.509 certificate credential from a classpath, filepath, or stream
 * resource. Supported certificate formats include: PEM, DER, and PKCS7.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class X509CertificateCredentialReader
  extends AbstractCredentialReader<X509Certificate>
{


  /** {@inheritDoc} */
  @Override
  public X509Certificate read(final InputStream is, final String... params)
    throws IOException, GeneralSecurityException
  {
    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return (X509Certificate) cf.generateCertificate(getBufferedInputStream(is));
  }
}
