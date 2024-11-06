/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.concurrent.CountDownLatch;
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
final class NettyUtils
{

  /** Time in milliseconds for graceful shutdown quiet period. */
  private static final long DEFAULT_SHUTDOWN_QUIET_PERIOD = 0;

  /** Time in milliseconds for graceful shutdown max wait. */
  private static final long DEFAULT_SHUTDOWN_MAX_TIMEOUT = 1000;

  /** Whether to use NIO even if other transports are available. */
  private static final boolean USE_NIO = Boolean.parseBoolean(
    System.getProperty("org.ldaptive.transport.netty.useNio", "false"));

  /** Whether Epoll is available. */
  private static final boolean EPOLL_AVAILABLE;

  /** Whether KQueue is available. */
  private static final boolean KQUEUE_AVAILABLE;

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyUtils.class);


  /** Default constructor. */
  private NettyUtils() {}


  static {
    boolean epollAvailable;
    try {
      Class.forName("io.netty.channel.epoll.Epoll");
      epollAvailable = Epoll.isAvailable();
    } catch (Exception e) {
      LOGGER.debug("Error detecting Epoll: {}:{}", e.getClass(), e.getMessage());
      epollAvailable = false;
    }
    EPOLL_AVAILABLE = epollAvailable;
    LOGGER.debug("Detected Epoll transport: {}", EPOLL_AVAILABLE);

    boolean kqueueAvailable;
    try {
      Class.forName("io.netty.channel.kqueue.KQueue");
      kqueueAvailable = KQueue.isAvailable();
    } catch (Exception e) {
      LOGGER.debug("Error detecting KQueue: {}:{}", e.getClass(), e.getMessage());
      kqueueAvailable = false;
    }
    KQUEUE_AVAILABLE = kqueueAvailable;
    LOGGER.debug("Detected KQueue transport: {}", KQUEUE_AVAILABLE);
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
    if (EPOLL_AVAILABLE && !USE_NIO) {
      return EpollSocketChannel.class;
    } else if (KQUEUE_AVAILABLE && !USE_NIO) {
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
    final String poolName = name.startsWith("ldaptive-") ? name : "ldaptive-" + name;
    if (EPOLL_AVAILABLE && !USE_NIO) {
      return new EpollEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(poolName, true, Thread.NORM_PRIORITY)));
    } else if (KQUEUE_AVAILABLE && !USE_NIO) {
      return new KQueueEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(poolName, true, Thread.NORM_PRIORITY)));
    } else {
      return new NioEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(poolName, true, Thread.NORM_PRIORITY)));
    }
  }


  /**
   * Invokes {@link EventLoopGroup#shutdownGracefully(long, long, TimeUnit)} on the supplied worker group. This method
   * blocks for twice the {@link #DEFAULT_SHUTDOWN_MAX_TIMEOUT} waiting for the shutdown to be done. If the future is
   * not invoked in that timeframe a warning is logged.
   *
   * @param  workerGroup  to shutdown
   */
  public static void shutdownGracefully(final EventLoopGroup workerGroup)
  {
    final CountDownLatch shutdownLatch = new CountDownLatch(1);
    workerGroup.shutdownGracefully(DEFAULT_SHUTDOWN_QUIET_PERIOD, DEFAULT_SHUTDOWN_MAX_TIMEOUT, TimeUnit.MILLISECONDS)
      .addListener(f -> {
        shutdownLatch.countDown();
        if (!f.isSuccess()) {
          if (f.cause() != null) {
            LOGGER.debug("Could not shutdown worker group {}", workerGroup, f.cause());
          } else {
            LOGGER.debug("Could not shutdown worker group {}", workerGroup);
          }
        } else {
          LOGGER.trace("worker group {} gracefully shutdown", workerGroup);
        }
      });
    try {
      if (!shutdownLatch.await(DEFAULT_SHUTDOWN_MAX_TIMEOUT * 2, TimeUnit.MILLISECONDS)) {
        LOGGER.debug("Shutdown max timeout was not honored for worker group {}", workerGroup);
      }
    } catch (InterruptedException e) {
      LOGGER.debug("Interrupted during shutdown for worker group {}", workerGroup);
    }
  }
}
