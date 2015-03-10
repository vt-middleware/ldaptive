/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Hostname verifier that returns true for any hostname. Use with caution.
 *
 * @author  Middleware Services
 */
public class AllowAnyHostnameVerifier implements HostnameVerifier, CertificateHostnameVerifier
{


  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    return true;
  }


  @Override
  public boolean verify(final String hostname, final X509Certificate cert)
  {
    return true;
  }
}
