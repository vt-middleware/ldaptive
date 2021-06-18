/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an {@link X509TrustManager} in order to provide hostname verification.
 *
 * @author  Middleware Services
 */
public class X509ExtendedTrustManagerWrapper extends X509ExtendedTrustManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Trust manager. */
  private final X509TrustManager trustManager;

  /** Hostname verifier. */
  private final CertificateHostnameVerifier hostnameVerifier;


  /**
   * Creates a new X509 extended trust manager wrapper.
   *
   * @param  manager  to wrap
   * @param  verifier  to verify hostname
   */
  public X509ExtendedTrustManagerWrapper(final X509TrustManager manager, final CertificateHostnameVerifier verifier)
  {
    trustManager = manager;
    hostnameVerifier = verifier;
  }


  /**
   * Resolves a hostname from the supplied session and invokes {@link #hostnameVerifier}.
   *
   * @param  session  to extract hostname from
   * @param  cert  to verify hostname against
   *
   * @throws  CertificateException  if the hostname cannot be verified
   */
  protected void verifyHostname(final SSLSession session, final X509Certificate cert)
    throws CertificateException
  {
    final HostnameResolver resolver = new HostnameResolver(session);
    final String hostname = resolver.resolve();
    if (!hostnameVerifier.verify(hostname, cert)) {
      throw new CertificateException("Hostname verification failed for " + hostname + " using " + hostnameVerifier);
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    if (trustManager instanceof X509ExtendedTrustManager) {
      ((X509ExtendedTrustManager) trustManager).checkClientTrusted(chain, authType, socket);
    } else {
      trustManager.checkClientTrusted(chain, authType);
      if (socket != null && socket.isConnected() && socket instanceof SSLSocket) {
        verifyHostname(((SSLSocket) socket).getHandshakeSession(), chain[0]);
      } else {
        throw new CertificateException("Could not retrieve SSL session from socket");
      }
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
    throws CertificateException
  {
    if (trustManager instanceof X509ExtendedTrustManager) {
      ((X509ExtendedTrustManager) trustManager).checkServerTrusted(chain, authType, socket);
    } else {
      trustManager.checkServerTrusted(chain, authType);
      if (socket != null && socket.isConnected() && socket instanceof SSLSocket) {
        verifyHostname(((SSLSocket) socket).getHandshakeSession(), chain[0]);
      } else {
        throw new CertificateException("Could not retrieve SSL session from socket");
      }
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    if (trustManager instanceof X509ExtendedTrustManager) {
      ((X509ExtendedTrustManager) trustManager).checkClientTrusted(chain, authType, engine);
    } else {
      trustManager.checkClientTrusted(chain, authType);
      verifyHostname(engine.getHandshakeSession(), chain[0]);
    }
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
    throws CertificateException
  {
    if (trustManager instanceof X509ExtendedTrustManager) {
      ((X509ExtendedTrustManager) trustManager).checkServerTrusted(chain, authType, engine);
    } else {
      trustManager.checkServerTrusted(chain, authType);
      verifyHostname(engine.getHandshakeSession(), chain[0]);
    }
  }


  @Override
  public void checkClientTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    trustManager.checkClientTrusted(chain, authType);
  }


  @Override
  public void checkServerTrusted(final X509Certificate[] chain, final String authType)
    throws CertificateException
  {
    trustManager.checkServerTrusted(chain, authType);
  }


  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return trustManager.getAcceptedIssuers();
  }
}
