/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;

/**
 * Base class for stateful thread pool strategy implementations. Maintains a reference to the I/O and message worker
 * groups so they can be reused for each connection. The {@link #shutdownOnClose} flag controls whether the worker
 * groups will be shutdown when {@link #close()} is invoked.
 *
 * @author  Middleware Services
 */
public abstract class AbstractStatefulThreadPoolStrategy extends AbstractThreadPoolStrategy
{

  /** Event loop group for I/O, must support the channel type. */
  private final EventLoopGroup ioWorkerGroup;

  /** Event loop group for message handling. */
  private final EventLoopGroup messageWorkerGroup;

  /** Whether to shutdown the event loop groups on {@link #close()}. */
  private boolean shutdownOnClose = true;


  /**
   * Creates a new abstract stateful thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pools
   * @param  ioGroup  thread pool used for I/O
   * @param  messageGroup  thread pool used for messages
   */
  public AbstractStatefulThreadPoolStrategy(
    final NettySocketType type,
    final String name,
    final EventLoopGroup ioGroup,
    final EventLoopGroup messageGroup)
  {
    super(type, name);
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
  public EventLoopGroup getIOThreadPool()
  {
    return ioWorkerGroup;
  }


  @Override
  public EventLoopGroup getMessageThreadPool()
  {
    return messageWorkerGroup;
  }


  @Override
  public boolean isShutdownOnConnectionClose()
  {
    return false;
  }


  @Override
  public void close()
  {
    if (shutdownOnClose) {
      try {
        if (!ioWorkerGroup.isShutdown()) {
          ioWorkerGroup.shutdownGracefully();
          logger.trace("Shutdown worker group {}", ioWorkerGroup);
        }
      } catch (Exception e) {
        logger.warn("Error shutting down the I/O worker group", e);
      }
      if (messageWorkerGroup != null && !messageWorkerGroup.isShutdown()) {
        try {
          messageWorkerGroup.shutdownGracefully();
          logger.trace("Shutdown worker group {}", messageWorkerGroup);
        } catch (Exception e) {
          logger.warn("Error shutting down the message worker group", e);
        }
      }
    }
  }
}
