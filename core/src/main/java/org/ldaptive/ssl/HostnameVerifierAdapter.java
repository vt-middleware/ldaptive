/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts a {@link CertificateHostnameVerifier} for use as a {@link HostnameVerifier}. This component can only be used
 * with a verified SSL session as it accesses the certificate from {@link SSLSession#getPeerCertificates()}.
 *
 * @author  Middleware Services
 */
public class HostnameVerifierAdapter implements HostnameVerifier
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Hostname verifier to adapt. */
  private final CertificateHostnameVerifier hostnameVerifier;


  /**
   * Creates a new hostname verifier adapter.
   *
   * @param  verifier  verifier to adapt
   */
  public HostnameVerifierAdapter(final CertificateHostnameVerifier verifier)
  {
    hostnameVerifier = verifier;
  }


  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    boolean b = false;
    if (session != null) {
      try {
        String name = null;
        if (hostname != null) {
          // if IPv6 strip off the "[]"
          if (hostname.startsWith("[") && hostname.endsWith("]")) {
            name = hostname.substring(1, hostname.length() - 1).trim();
          } else {
            name = hostname.trim();
          }
        }
        b = hostnameVerifier.verify(name, (X509Certificate) session.getPeerCertificates()[0]);
      } catch (SSLPeerUnverifiedException e) {
        logger.warn("Could not get certificate from SSL session {} for hostname {}", session, hostname, e);
      }
    }
    return b;
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" + "hostnameVerifier=" + hostnameVerifier + "]";
  }
}
