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
   * Returns the socket channel type for the supplied type.
   *
   * @param  type  socket type enum
   *
   * @return  socket channel class type
   */
  public static Class<? extends Channel> getSocketChannelType(final NettySocketType type)
  {
    final NettySocketType st;
    if (type == NettySocketType.DEFAULT) {
      st = NettySocketType.getDefaultSocketType();
    } else {
      st = type;
    }
    final Class<? extends Channel> socketClass;
    switch(st) {
    case EPOLL:
      socketClass = EpollSocketChannel.class;
      break;
    case KQUEUE:
      socketClass = KQueueSocketChannel.class;
      break;
    case NIO:
    default:
      socketClass = NioSocketChannel.class;
      break;
    }
    return socketClass;
  }


  /**
   * Returns the default event loop group for this platform. See {@link Epoll#isAvailable()} and {@link
   * KQueue#isAvailable()}. Set numThreads to zero to use the netty default.
   *
   * @param  type  socket type enum
   * @param  name  of the thread pool
   * @param  numThreads  number of threads in the thread pool
   *
   * @return  event loop group
   */
  public static EventLoopGroup createEventLoopGroup(final NettySocketType type, final String name, final int numThreads)
  {
    final NettySocketType st;
    if (type == NettySocketType.DEFAULT) {
      st = NettySocketType.getDefaultSocketType();
    } else {
      st = type;
    }
    final EventLoopGroup loopGroup;
    switch(st) {
    case EPOLL:
      loopGroup = new EpollEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
      break;
    case KQUEUE:
      loopGroup = new KQueueEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
      break;
    case NIO:
    default:
      loopGroup = new NioEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(name, true, Thread.NORM_PRIORITY)));
      break;
    }
    return loopGroup;
  }
}
