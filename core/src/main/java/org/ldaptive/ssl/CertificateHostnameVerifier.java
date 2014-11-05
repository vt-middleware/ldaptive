/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.X509Certificate;

/**
 * Interface for verifying a hostname matching a certificate.
 *
 * @author  Middleware Services
 */
public interface CertificateHostnameVerifier
{


  /**
   * Verify the supplied hostname matches the supplied certificate.
   *
   * @param  hostname  to verify
   * @param  cert  to verify hostname against
   *
   * @return  whether hostname is valid for the supplied certificate
   */
  boolean verify(String hostname, X509Certificate cert);
}
