/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.Map;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
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
  private int numIoThreads;

  /** Number of message threads. */
  private int numMessageThreads = -1;

  /** Override channel options. */
  private final Map<ChannelOption, Object> channelOptions;


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
    this(ioThreads, null);
  }


  /**
   * Creates a new connection transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  options  netty channel options
   */
  public ConnectionTransport(final int ioThreads, final Map<ChannelOption, Object> options)
  {
    numIoThreads = ioThreads;
    channelOptions = options;
  }


  /**
   * Creates a new connection transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public ConnectionTransport(final int ioThreads, final int messageThreads)
  {
    this(ioThreads, messageThreads, null);
  }


  /**
   * Creates a new connection transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   * @param  options  netty channel options
   */
  public ConnectionTransport(
    final int ioThreads,
    final int messageThreads,
    final Map<ChannelOption, Object> options)
  {
    numIoThreads = ioThreads;
    numMessageThreads = messageThreads;
    channelOptions = options;
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
        createEventLoopGroup(ConnectionTransport.class.getSimpleName(), numIoThreads),
        createEventLoopGroup(ConnectionTransport.class.getSimpleName(), numMessageThreads),
        channelOptions,
        true);
    }
    return new NettyConnection(
      cc,
      getSocketChannelType(),
      createEventLoopGroup(ConnectionTransport.class.getSimpleName(), numIoThreads),
      null,
      channelOptions,
      true);
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("numIoThreads=").append(numIoThreads).append(", ")
      .append("numMessageThreads=").append(numMessageThreads).append(", ")
      .append("channelOptions=").append(channelOptions).append("]").toString();
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
