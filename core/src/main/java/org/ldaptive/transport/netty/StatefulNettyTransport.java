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
 * Creates netty connections with configured event loops. This implementation reuses the same event loops for each
 * connection created. Event loop groups are not shutdown when the connection closed, they can be shared between
 * multiple connections. {@link #shutdownOnClose} controls whether event loop groups are shutdown when the transport is
 * closed.
 *
 * @author  Middleware Services
 */
class StatefulNettyTransport implements Transport
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Channel type. */
  private final Class<? extends Channel> channelType;

  /** Event loop group for I/O, must support the channel type. */
  private final EventLoopGroup ioWorkerGroup;

  /** Event loop group for message handling. */
  private final EventLoopGroup messageWorkerGroup;

  /** Whether to shut down the event loop groups on {@link #close()}. */
  private final boolean shutdownOnClose;


  /**
   * Creates a new stateful netty transport.
   */
  StatefulNettyTransport()
  {
    this(0);
  }


  /**
   * Creates a new stateful netty transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  StatefulNettyTransport(final int ioThreads)
  {
    this(null, ioThreads, -1, true);
  }


  /**
   * Creates a new stateful netty transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  StatefulNettyTransport(final int ioThreads, final int messageThreads)
  {
    this(null, ioThreads, messageThreads, true);
  }


  /**
   * Creates a new stateful netty transport.
   *
   * @param  name  to assign the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   * @param  shutdown  whether to shut down the event loop groups on close
   */
  StatefulNettyTransport(
    final String name, final int ioThreads, final int messageThreads, final boolean shutdown)
  {
    this(
      NettyUtils.getDefaultSocketChannelType(),
      NettyUtils.createDefaultEventLoopGroup(
        (name == null ? "stateful-netty-transport" : name) + "-io", ioThreads),
      messageThreads == -1 ? null :
        NettyUtils.createDefaultEventLoopGroup(
          (name == null ? "stateful-netty-transport" : name) + "-messages", messageThreads),
      shutdown);
  }


  /**
   * Creates a new netty connection factory transport.
   *
   * @param  type  of channel
   * @param  ioGroup  event loop group to handle I/O
   * @param  messageGroup  event loop group to handle inbound messages, can be null
   * @param  shutdown  whether to shut down the event loop groups on close
   */
  StatefulNettyTransport(
    final Class<? extends Channel> type,
    final EventLoopGroup ioGroup,
    final EventLoopGroup messageGroup,
    final boolean shutdown)
  {
    channelType = type;
    ioWorkerGroup = ioGroup;
    messageWorkerGroup = messageGroup;
    shutdownOnClose = shutdown;
  }


  @Override
  public Connection create(final ConnectionConfig cc)
  {
    return new NettyConnection(cc, channelType, ioWorkerGroup, messageWorkerGroup, false);
  }


  @Override
  public void close()
  {
    if (shutdownOnClose) {
      shutdown();
    }
  }


  @Override
  public void shutdown()
  {
    if (!ioWorkerGroup.isShutdown()) {
      NettyUtils.shutdownGracefully(ioWorkerGroup);
      logger.trace("shutdown worker group {}", ioWorkerGroup);
    }
    if (messageWorkerGroup != null && !messageWorkerGroup.isShutdown()) {
      NettyUtils.shutdownGracefully(messageWorkerGroup);
      logger.trace("shutdown worker group {}", messageWorkerGroup);
    }
  }


  /**
   * Returns whether thread pools will be shutdown on close.
   *
   * @return  whether thread pools will be shutdown on close
   */
  boolean getShutdownOnClose()
  {
    return shutdownOnClose;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "channelType=" + channelType + ", " +
      "ioWorkerGroup=" + ioWorkerGroup + ", " +
      "messageWorkerGroup=" + messageWorkerGroup + ", " +
      "shutdownOnClose=" + shutdownOnClose + "]";
  }
}
