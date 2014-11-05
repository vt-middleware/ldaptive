/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ldapi;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLDecoder;
import javax.net.SocketFactory;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

/**
 * AFUnixSocketFactory is an extension of SocketFactory which utilizes {@link
 * AFUNIXSocket} to connect to a host, it is intended for local file-based
 * native socket connections only.
 *
 * @author  Middleware Services
 * @version  $Revision: 3008 $ $Date: 2014-07-02 10:36:23 -0400 (Wed, 02 Jul 2014) $
 */
public class AFUnixSocketFactory extends SocketFactory
{

  /** File system location of the domain socket. */
  private final String socketFile;


  /**
   * Creates a new AFUnixSocketFactory configured with the system property
   * 'org.ldaptive.ldapi.socketFile'.
   */
  public AFUnixSocketFactory()
  {
    this(System.getProperty("org.ldaptive.ldapi.socketFile"));
  }


  /**
   * Creates a new AFUnixSocketFactory.
   *
   * @param  socket  file system location of the domain socket
   */
  public AFUnixSocketFactory(final String socket)
  {
    socketFile = socket;
  }


  /**
   * This returns a new instance of AFUnixSocketFactory. See {@link
   * #AFUnixSocketFactory()}.
   *
   * @return  socket factory
   */
  public static SocketFactory getDefault()
  {
    return new AFUnixSocketFactory();
  }


  /**
   * Creates a new instance of AFUNIXSocket using the host as the file system
   * location of the domain socket. The host parameter is ignored if {@link
   * #socketFile} is set. The port parameter is always ignored.
   *
   * @param  host  ignored if {@link #socketFile} is set
   * @param  port  Unsupported, will be ignored
   *
   * @return  unix socket
   *
   * @throws  IOException  if the file system location cannot be URL decoded
   */
  @Override
  public Socket createSocket(final String host, final int port)
    throws IOException
  {
    File file;
    if (socketFile != null) {
      file = new File(URLDecoder.decode(socketFile, "UTF-8"));
    } else {
      file = new File(URLDecoder.decode(host, "UTF-8"));
    }

    final AFUNIXSocketAddress localAddress = new AFUNIXSocketAddress(file);
    final AFUNIXSocket sock = AFUNIXSocket.newInstance();
    sock.connect(localAddress);
    return sock;
  }


  /**
   * This method is not supported.
   *
   * @param  host  Unsupported.
   * @param  port  Unsupported.
   * @param  localHost  Unsupported.
   * @param  localPort  Unsupported.
   *
   * @return  Socket Unsupported.
   *
   * @throws  IOException  Unsupported.
   */
  @Override
  public Socket createSocket(
    final String host,
    final int port,
    final InetAddress localHost,
    final int localPort)
    throws IOException
  {
    throw new UnsupportedOperationException("This method is not supported.");
  }


  /**
   * This method is not supported.
   *
   * @param  host  Unsupported.
   * @param  port  Unsupported.
   *
   * @return  Socket Unsupported.
   *
   * @throws  IOException  Unsupported.
   */
  @Override
  public Socket createSocket(final InetAddress host, final int port)
    throws IOException
  {
    throw new UnsupportedOperationException("This method is not supported.");
  }


  /**
   * This method is not supported.
   *
   * @param  address  Unsupported.
   * @param  port  Unsupported.
   * @param  localAddress  Unsupported.
   * @param  localPort  Unsupported.
   *
   * @return  Socket Unsupported.
   *
   * @throws  IOException  Unsupported.
   */
  @Override
  public Socket createSocket(
    final InetAddress address,
    final int port,
    final InetAddress localAddress,
    final int localPort)
    throws IOException
  {
    throw new UnsupportedOperationException("This method is not supported.");
  }
}
