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
 * AFUnixSocketFactory is an extension of SocketFactory which utilizes {@link AFUNIXSocket} to connect to a host, it is
 * intended for local file-based unix native socket connections only.
 *
 * @author  Middleware Services
 */
public class AFUnixSocketFactory extends SocketFactory
{

  /** System parameter name specifying location of the socket file. */
  public static final String SOCKET_FILE_PROPERTY = "org.ldaptive.ldapi.socketFile";

  /** File system location of the domain socket.  Supplied either via constructor, or {@link #SOCKET_FILE_PROPERTY} */
  private final String socketFile;


  /** Creates a new AFUnixSocketFactory configured with the system property {@link #SOCKET_FILE_PROPERTY}. */
  public AFUnixSocketFactory()
  {
    this(System.getProperty(SOCKET_FILE_PROPERTY));
  }


  /**
   * Creates a new AFUnixSocketFactory with a given file path.
   *
   * @param  socket  file system location of the domain socket
   */
  public AFUnixSocketFactory(final String socket)
  {
    socketFile = socket;
  }


  /**
   * This returns a new instance of AFUnixSocketFactory. See {@link #AFUnixSocketFactory()}.
   *
   * @return  socket factory
   */
  public static SocketFactory getDefault()
  {
    return new AFUnixSocketFactory();
  }


  /**
   * Uses the {@link #socketFile} property as the path to initiate a file socket ignoring all parameters of this
   * method.
   *
   * @param  host  Unsupported, will be ignored
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
    final File file;
    if (socketFile == null) {
      throw new IOException("socketFile (specified in org.ldaptive.ldapi.socketFile" +
              " or passed through factory constructor) MUST be specified to call this method.");
    }
    file = new File(URLDecoder.decode(socketFile, "UTF-8"));
    final AFUNIXSocketAddress localAddress = new AFUNIXSocketAddress(file);
    final AFUNIXSocket sock = AFUNIXSocket.newInstance();
    sock.connect(localAddress);
    return sock;
  }


  /**
   * Uses the {@link #socketFile} property as the path to initiate a file socket ignoring all parameters of this
   * method.
   *
   * @param  host  Unsupported, will be ignored
   * @param  port  Unsupported, will be ignored
   * @param  localHost  Unsupported, will be ignored
   * @param  localPort  Unsupported, will be ignored
   *
   * @return  unix socket
   *
   * @throws  IOException If socketFile is not specified or an underlying error occurs.
   */
  @Override
  public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
    throws IOException
  {
    return createSocket(socketFile, -1);
  }


  /**
   * Uses the {@link #socketFile} property as the path to initiate a file socket ignoring all parameters of this
   * method.
   *
   * @param  host  Unsupported, will be ignored
   * @param  port  Unsupported, will be ignored
   *
   * @return  unix socket
   *
   * @throws  IOException If socketFile is not specified or an underlying error occurs.
   */
  @Override
  public Socket createSocket(final InetAddress host, final int port)
    throws IOException
  {
    return createSocket(socketFile, -1);
  }


  /**
   * Uses the {@link #socketFile} property as the path to initiate a file socket ignoring all parameters of this
   * method.
   * @param  address  Unsupported, will be ignored
   * @param  port  Unsupported, will be ignored
   * @param  localAddress  Unsupported, will be ignored
   * @param  localPort  Unsupported, will be ignored
   *
   * @return  unix socket
   *
   * @throws  IOException If socketFile is not specified or an underlying error occurs.
   */
  @Override
  public Socket createSocket(
    final InetAddress address,
    final int port,
    final InetAddress localAddress,
    final int localPort)
    throws IOException
  {
    return createSocket(socketFile, -1);
  }
}
