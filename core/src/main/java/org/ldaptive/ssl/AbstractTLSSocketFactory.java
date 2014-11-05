/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Provides common implementation for TLSSocketFactory.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractTLSSocketFactory extends SSLSocketFactory
{

  /** Default SSL protocol, value is {@value}. */
  public static final String DEFAULT_PROTOCOL = "TLS";

  /** SSLSocketFactory used for creating SSL sockets. */
  protected SSLSocketFactory factory;

  /** SSL configuration options. */
  private SslConfig sslConfig;

  /** Hostname verifier. */
  private HostnameVerifier hostnameVerifier;

  /** Socket configuration options. */
  private SocketConfig socketConfig;


  /**
   * Prepares this socket factory for use. Must be called before factory can be
   * used.
   *
   * @throws  GeneralSecurityException  if the factory cannot be initialized
   */
  public abstract void initialize()
    throws GeneralSecurityException;


  /**
   * Returns the underlying SSL socket factory that this class uses for creating
   * SSL Sockets.
   *
   * @return  SSL socket factory
   */
  public SSLSocketFactory getFactory()
  {
    return factory;
  }


  /**
   * Returns the SSL configuration used by this socket factory.
   *
   * @return  ssl config
   */
  public SslConfig getSslConfig()
  {
    return sslConfig;
  }


  /**
   * Sets the SSL configuration used by this socket factory.
   *
   * @param  config  ssl config
   */
  public void setSslConfig(final SslConfig config)
  {
    sslConfig = config;
  }


  /**
   * Returns the hostname verifier.
   *
   * @return  trust managers
   */
  public HostnameVerifier getHostnameVerifier()
  {
    return hostnameVerifier;
  }


  /**
   * Sets the hostname verifier.
   *
   * @param  verifier  hostname verifier
   */
  public void setHostnameVerifier(final HostnameVerifier verifier)
  {
    hostnameVerifier = verifier;
  }


  /**
   * Returns the socket configuration used by this socket factory.
   *
   * @return  socket config
   */
  public SocketConfig getSocketConfig()
  {
    return socketConfig;
  }


  /**
   * Sets the socket configuration used by this socket factory.
   *
   * @param  config  socket config
   */
  public void setSocketConfig(final SocketConfig config)
  {
    socketConfig = config;
  }


  /**
   * Initializes the supplied socket for use.
   *
   * @param  socket  SSL socket to initialize
   *
   * @return  SSL socket
   *
   * @throws  IOException  if an I/O error occurs when initializing the socket
   */
  protected SSLSocket initSSLSocket(final SSLSocket socket)
    throws IOException
  {
    final SocketConfig socketC = getSocketConfig();
    if (socketC != null) {
      socketC.configureSocket(socket);
    }

    final SslConfig sslC = getSslConfig();
    if (sslC != null) {
      if (sslC.getEnabledCipherSuites() != null) {
        socket.setEnabledCipherSuites(sslC.getEnabledCipherSuites());
      }
      if (sslC.getEnabledProtocols() != null) {
        socket.setEnabledProtocols(sslC.getEnabledProtocols());
      }
      if (sslC.getHandshakeCompletedListeners() != null) {
        for (HandshakeCompletedListener listener :
             sslC.getHandshakeCompletedListeners()) {
          socket.addHandshakeCompletedListener(listener);
        }
      }
    }
    if (hostnameVerifier != null) {
      // calling getSession() will initiate the handshake if necessary
      final String hostname = socket.getSession().getPeerHost();
      if (!hostnameVerifier.verify(hostname, socket.getSession())) {
        socket.close();
        socket.getSession().invalidate();
        throw new SSLPeerUnverifiedException(
          String.format(
            "Hostname '%s' does not match the hostname in the server's " +
            "certificate",
            hostname));
      }
    }
    return socket;
  }


  /**
   * Returns a socket layered over an existing socket connected to the named
   * host, at the given port.
   *
   * @param  socket  existing socket
   * @param  host  server hostname
   * @param  port  server port
   * @param  autoClose  close the underlying socket when this socket is closed
   *
   * @return  socket connected to the specified host and port
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket(
    final Socket socket,
    final String host,
    final int port,
    final boolean autoClose)
    throws IOException
  {
    return
      initSSLSocket(
        (SSLSocket) factory.createSocket(socket, host, port, autoClose));
  }


  /**
   * Creates an unconnected socket.
   *
   * @return  unconnected socket
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket()
    throws IOException
  {
    return initSSLSocket((SSLSocket) factory.createSocket());
  }


  /**
   * Creates a socket and connects it to the specified port number at the
   * specified address.
   *
   * @param  host  server hostname
   * @param  port  server port
   *
   * @return  socket connected to the specified host and port
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket(final InetAddress host, final int port)
    throws IOException
  {
    return initSSLSocket((SSLSocket) factory.createSocket(host, port));
  }


  /**
   * Creates a socket and connect it to the specified port number at the
   * specified address. The socket will also be bound to the supplied local
   * address and port.
   *
   * @param  address  server hostname
   * @param  port  server port
   * @param  localAddress  client hostname
   * @param  localPort  client port
   *
   * @return  socket connected to the specified host and port
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket(
    final InetAddress address,
    final int port,
    final InetAddress localAddress,
    final int localPort)
    throws IOException
  {
    return
      initSSLSocket(
        (SSLSocket) factory.createSocket(
          address,
          port,
          localAddress,
          localPort));
  }


  /**
   * Creates a socket and connects it to the specified port number at the
   * specified address.
   *
   * @param  host  server hostname
   * @param  port  server port
   *
   * @return  socket connected to the specified host and port
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket(final String host, final int port)
    throws IOException
  {
    return initSSLSocket((SSLSocket) factory.createSocket(host, port));
  }


  /**
   * Creates a socket and connect it to the specified port number at the
   * specified address. The socket will also be bound to the supplied local
   * address and port.
   *
   * @param  host  server hostname
   * @param  port  server port
   * @param  localHost  client hostname
   * @param  localPort  client port
   *
   * @return  socket connected to the specified host and port
   *
   * @throws  IOException  if an I/O error occurs when creating the socket
   */
  @Override
  public Socket createSocket(
    final String host,
    final int port,
    final InetAddress localHost,
    final int localPort)
    throws IOException
  {
    return
      initSSLSocket(
        (SSLSocket) factory.createSocket(host, port, localHost, localPort));
  }


  /**
   * Returns the list of cipher suites which are enabled by default.
   *
   * @return  cipher suites
   */
  @Override
  public String[] getDefaultCipherSuites()
  {
    return factory.getDefaultCipherSuites();
  }


  /**
   * Returns the names of the cipher suites which could be enabled for use on an
   * SSL connection.
   *
   * @return  cipher suites
   */
  @Override
  public String[] getSupportedCipherSuites()
  {
    return factory.getSupportedCipherSuites();
  }
}
