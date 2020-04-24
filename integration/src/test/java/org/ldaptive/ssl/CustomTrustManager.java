/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * Trust manager for testing.
 *
 * @author  Middleware Services
 */
public class CustomTrustManager implements X509TrustManager
{


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    if (chain.length != 1) {
      throw new CertificateException("Wrong chain length: " + chain.length);
    }
  }

  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    if (chain.length != 1) {
      throw new CertificateException("Wrong chain length: " + chain.length);
    }
  }

  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
