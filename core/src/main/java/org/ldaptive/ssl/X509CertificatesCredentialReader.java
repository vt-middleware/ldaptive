/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;


import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Loads X.509 certificate credentials from a classpath, filepath, or stream resource.
 * When working with filepath, multiple files may be separated using a comma (i.e. {@code cert1.pem,cert2.crt}).
 * Supported certificate formats include: PEM, DER, and PKCS7.
 *
 * @author  Middleware Services
 */
public class X509CertificatesCredentialReader extends AbstractCredentialReader<X509Certificate[]>
{

  @Override
  public X509Certificate[] read(final String path, final String... params)
      throws IOException, GeneralSecurityException
  {
    final String[] paths = path.split(",");
    return Arrays.stream(paths)
        .map(individualPath -> {
          try {
            return Arrays.asList(super.read(individualPath, params));
          } catch (final Exception e) {
            logger.warn(String.format("Unable to read certificate at %s", individualPath), e);
          }
          return null;
        })
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .toArray(X509Certificate[]::new);
  }
  
  @Override
  public X509Certificate[] read(final InputStream is, final String... params)
    throws IOException, GeneralSecurityException
  {
    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    final List<X509Certificate> certList = new ArrayList<>();
    final InputStream bufIs = getBufferedInputStream(is);
    while (bufIs.available() > 0) {
      final X509Certificate cert = (X509Certificate) cf.generateCertificate(bufIs);
      if (cert != null) {
        certList.add(cert);
      }
    }
    return certList.toArray(new X509Certificate[0]);
  }
}
