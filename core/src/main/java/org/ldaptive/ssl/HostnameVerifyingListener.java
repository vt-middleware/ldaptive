/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handshake completed listener that invokes a hostname verifier. If hostname
 * verification fails, the socket is closed and the SSL session is invalidated.
 *
 * @author  Middleware Services
 */
public class HostnameVerifyingListener implements HandshakeCompletedListener
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Hostname verifier invoked when the handshake completes. */
  private final HostnameVerifier hostnameVerifier;

  /** Whether this listener has been invoked. */
  private boolean invoked;

  /** Whether hostname verification succeeded. */
  private boolean verified;

  /** Hostname used in verification. */
  private String hostname;


  /**
   * Creates a new verifying handshake completed listener. Hostname will be
   * derived from the SSL session.
   *
   * @param  verifier  hostname verifier
   */
  public HostnameVerifyingListener(final HostnameVerifier verifier)
  {
    hostnameVerifier = verifier;
  }


  /**
   * Creates a new verifying handshake completed listener.
   *
   * @param  verifier  hostname verifier
   * @param  name  hostname to verify
   */
  public HostnameVerifyingListener(
    final HostnameVerifier verifier,
    final String name)
  {
    hostnameVerifier = verifier;
    hostname = name;
  }


  @Override
  public void handshakeCompleted(final HandshakeCompletedEvent event)
  {
    invoked = true;
    if (hostname == null) {
      hostname = event.getSession().getPeerHost();
    }
    if (!hostnameVerifier.verify(hostname, event.getSession())) {
      try {
        event.getSocket().close();
      } catch (IOException e) {
        logger.warn("Error closing SSL socket", e);
      }
      event.getSession().invalidate();
    } else {
      verified = true;
    }
  }


  /**
   * Throws exception if hostname verification failed.
   *
   * @throws  IllegalStateException  if this listener has not been invoked
   * @throws  SSLPeerUnverifiedException  if the hostname failed to verify
   */
  public void peerVerified()
    throws SSLPeerUnverifiedException
  {
    if (!invoked) {
      throw new IllegalStateException("Handshake has not completed");
    }
    if (!verified) {
      throw new SSLPeerUnverifiedException(
        String.format(
          "Hostname '%s' does not match the hostname in the server's " +
          "certificate",
          hostname));
    }
  }
}
