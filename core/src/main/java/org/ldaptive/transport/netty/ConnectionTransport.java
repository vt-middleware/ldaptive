/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates netty connections using the best fit event loop group based on the operating system. See {@link
 * io.netty.channel.epoll.Epoll#isAvailable()} and {@link io.netty.channel.kqueue.KQueue#isAvailable()}. The event loop
 * group is shutdown when the connection is closed.
 *
 * @author  Middleware Services
 */
public class ConnectionTransport implements Transport
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Number of I/O threads. */
  private final int numIoThreads;

  /** Number of message threads. */
  private final int numMessageThreads;


  /**
   * Creates a new connection transport.
   */
  public ConnectionTransport()
  {
    this(0);
  }


  /**
   * Creates a new connection transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public ConnectionTransport(final int ioThreads)
  {
    this(ioThreads, -1);
  }


  /**
   * Creates a new connection transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public ConnectionTransport(final int ioThreads, final int messageThreads)
  {
    numIoThreads = ioThreads;
    numMessageThreads = messageThreads;
  }


  /**
   * Returns the socket channel type used with the event loop group.
   *
   * @return  socket channel type
   */
  protected Class<? extends Channel> getSocketChannelType()
  {
    return NettyUtils.getDefaultSocketChannelType();
  }


  /**
   * Returns a new event loop group with the supplied name and number of threads.
   *
   * @param  name  of the event loop group
   * @param  numThreads  number of worker threads
   *
   * @return  new event loop group
   */
  protected EventLoopGroup createEventLoopGroup(final String name, final int numThreads)
  {
    return NettyUtils.createDefaultEventLoopGroup(name, numThreads);
  }


  @Override
  public Connection create(final ConnectionConfig cc)
  {
    if (numMessageThreads != -1) {
      return new NettyConnection(
        cc,
        getSocketChannelType(),
        createEventLoopGroup(getClass().getSimpleName() + "@" + hashCode() + "-io", numIoThreads),
        createEventLoopGroup(getClass().getSimpleName() + "@" + hashCode() + "-messages", numMessageThreads),
        true);
    }
    return new NettyConnection(
      cc,
      getSocketChannelType(),
      createEventLoopGroup(getClass().getSimpleName() + "@" + hashCode() + "-io", numIoThreads),
      null,
      true);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "numIoThreads=" + numIoThreads + ", " +
      "numMessageThreads=" + numMessageThreads + "]";
  }


  /** A {@link ConnectionTransport} configured with a single underlying thread. */
  public static class SingleThread extends ConnectionTransport
  {


    /**
     * Default constructor.
     */
    public SingleThread()
    {
      super(1);
    }
  }


  /** A {@link ConnectionTransport} configured with two underlying threads. */
  public static class DualThread extends ConnectionTransport
  {


    /**
     * Default constructor.
     */
    public DualThread()
    {
      super(2);
    }
  }
}
