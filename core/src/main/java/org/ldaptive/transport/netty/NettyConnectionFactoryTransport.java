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
 * connection created.
 *
 * @author  Middleware Services
 */
public class NettyConnectionFactoryTransport implements Transport
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Channel type. */
  private final Class<? extends Channel> channelType;

  /** Event loop group for I/O, must support the channel type. */
  private final EventLoopGroup ioWorkerGroup;

  /** Event loop group for message handling. */
  private final EventLoopGroup messageWorkerGroup;

  /** Whether to shutdown the event loop groups on {@link #close()}. */
  private boolean shutdownOnClose = true;


  /**
   * Creates a new netty connection factory transport.
   *
   * @param  type  of channel
   * @param  ioGroup  event loop group to handle I/O
   */
  public NettyConnectionFactoryTransport(final Class<? extends Channel> type, final EventLoopGroup ioGroup)
  {
    this(type, ioGroup, null);
  }


  /**
   * Creates a new netty connection factory transport.
   *
   * @param  type  of channel
   * @param  ioGroup  event loop group to handle I/O
   * @param  messageGroup  event loop group to handle inbound messages, can be null
   */
  public NettyConnectionFactoryTransport(
    final Class<? extends Channel> type,
    final EventLoopGroup ioGroup,
    final EventLoopGroup messageGroup)
  {
    channelType = type;
    ioWorkerGroup = ioGroup;
    messageWorkerGroup = messageGroup;
  }


  /**
   * Sets whether to shutdown the event loop groups on close.
   *
   * @param  b  whether to shutdown on close
   */
  public void setShutdownOnClose(final boolean b)
  {
    shutdownOnClose = b;
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
      if (!ioWorkerGroup.isShutdown()) {
        NettyUtils.shutdownGracefully(ioWorkerGroup);
        logger.trace("shutdown worker group {}", ioWorkerGroup);
      }
      if (messageWorkerGroup != null && !messageWorkerGroup.isShutdown()) {
        NettyUtils.shutdownGracefully(messageWorkerGroup);
        logger.trace("shutdown worker group {}", messageWorkerGroup);
      }
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("channelType=").append(channelType).append(", ")
      .append("ioWorkerGroup=").append(ioWorkerGroup).append(", ")
      .append("messageWorkerGroup=").append(messageWorkerGroup).append(", ")
      .append("shutdownOnClose=").append(shutdownOnClose).append("]").toString();
  }
}
