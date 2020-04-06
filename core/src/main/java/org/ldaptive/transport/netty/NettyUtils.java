/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.concurrent.TimeUnit;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class NettyUtils
{

  /** Time in milliseconds for graceful shutdown quiet period. */
  private static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 500;

  /** Time in milliseconds for graceful shutdown max wait. */
  private static final long DEFAULT_SHUTDOWN_MAX_TIMEOUT = 1000;

  /** Whether to use NIO even if other transports are available. */
  private static final boolean USE_NIO = Boolean.valueOf(
    System.getProperty("org.ldaptive.transport.netty.useNio", "false"));

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyUtils.class);


  /** Default constructor. */
  private NettyUtils() {}


  // emit a log message for the detected transport type
  static {
    LOGGER.debug("Detecting Epoll transport: {}", Epoll.isAvailable());
    LOGGER.debug("Detecting KQueue transport: {}", KQueue.isAvailable());
    LOGGER.debug("Overriding to use Nio transport: {}", USE_NIO);
  }


  /**
   * Returns the default socket channel type for this platform. See {@link Epoll#isAvailable()} and {@link
   * KQueue#isAvailable()}.
   *
   * @return  socket channel type
   */
  public static Class<? extends Channel> getDefaultSocketChannelType()
  {
    if (Epoll.isAvailable() && !USE_NIO) {
      return EpollSocketChannel.class;
    } else if (KQueue.isAvailable() && !USE_NIO) {
      return KQueueSocketChannel.class;
    } else {
      return NioSocketChannel.class;
    }
  }


  /**
   * Returns the default event loop group for this platform. See {@link Epoll#isAvailable()} and {@link
   * KQueue#isAvailable()}. Set numThreads to zero to use the netty default.
   *
   * @param  name  of the thread pool
   * @param  numThreads  number of threads in the thread pool
   * @return  event loop group
   */
  public static EventLoopGroup createDefaultEventLoopGroup(final String name, final int numThreads)
  {
    if (Epoll.isAvailable() && !USE_NIO) {
      return new EpollEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    } else if (KQueue.isAvailable() && !USE_NIO) {
      return new KQueueEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    } else {
      return new NioEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    }
  }


  /**
   * Invokes {@link EventLoopGroup#shutdownGracefully(long, long, TimeUnit)} on the supplied worker group. This method
   * is asynchronous.
   *
   * @param  workerGroup  to shutdown
   */
  public static void shutdownGracefully(final EventLoopGroup workerGroup)
  {
    workerGroup.shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_MAX_TIMEOUT, TimeUnit.MILLISECONDS)
      .addListener(f -> {
        if (!f.isSuccess()) {
          if (f.cause() != null) {
            LOGGER.warn("Could not shutdown worker group {}", workerGroup, f.cause());
          } else {
            LOGGER.warn("Could not shutdown worker group {}", workerGroup);
          }
        } else {
          LOGGER.trace("Worker group {} gracefully shutdown", workerGroup);
        }
      });
  }
}
