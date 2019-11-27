/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

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

/**
 * Provides utility methods for this package.
 *
 * @author  Middleware Services
 */
public final class NettyUtils
{


  /** Default constructor. */
  private NettyUtils() {}


  /**
   * Returns the default socket channel type for this platform. See {@link Epoll#isAvailable()} and {@link
   * KQueue#isAvailable()}.
   *
   * @return  socket channel type
   */
  public static Class<? extends Channel> getDefaultSocketChannelType()
  {
    if (Epoll.isAvailable()) {
      return EpollSocketChannel.class;
    } else if (KQueue.isAvailable()) {
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
    if (Epoll.isAvailable()) {
      return new EpollEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    } else if (KQueue.isAvailable()) {
      return new KQueueEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    } else {
      return new NioEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
    }
  }
}
