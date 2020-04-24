/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * Trust manager that trusts any certificate. Use with caution.
 *
 * @author  Middleware Services
 */
public class AllowAnyTrustManager extends X509ExtendedTrustManager
{


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException {}


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException {}


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException {}


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException {}


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException {}


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException {}
}
