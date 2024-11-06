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
 * io.netty.channel.epoll.Epoll#isAvailable()} and {@link io.netty.channel.kqueue.KQueue#isAvailable()}. New event loop
 * groups are created for every connection. The event loop groups are shutdown when the connection is closed.
 *
 * @author  Middleware Services
 */
class DefaultNettyTransport implements Transport
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Name of the event loop group. */
  private final String threadPoolName;

  /** Number of I/O threads. */
  private final int numIoThreads;

  /** Number of message threads. */
  private final int numMessageThreads;


  /**
   * Creates a new default netty transport.
   */
  DefaultNettyTransport()
  {
    this(0);
  }


  /**
   * Creates a new default netty transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  DefaultNettyTransport(final int ioThreads)
  {
    this(null, ioThreads, -1);
  }


  /**
   * Creates a new default netty transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  DefaultNettyTransport(final int ioThreads, final int messageThreads)
  {
    this(null, ioThreads, messageThreads);
  }


  /**
   * Creates a new default netty transport.
   *
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  DefaultNettyTransport(final String name, final int ioThreads, final int messageThreads)
  {
    threadPoolName = name == null ? "default-netty-transport" : name;
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
        createEventLoopGroup(threadPoolName + "-io", numIoThreads),
        createEventLoopGroup(threadPoolName + "-messages", numMessageThreads),
        true);
    }
    return new NettyConnection(
      cc,
      getSocketChannelType(),
      createEventLoopGroup(threadPoolName + "-io", numIoThreads),
      null,
      true);
  }


  @Override
  public void close() {}


  @Override
  public void shutdown() {}


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "threadPoolName" + threadPoolName + ", " +
      "numIoThreads=" + numIoThreads + ", " +
      "numMessageThreads=" + numMessageThreads + "]";
  }
}
