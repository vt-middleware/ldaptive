/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using an {@link EpollEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class EpollTransport extends NettyTransport
{


  /**
   * Creates a new Epoll transport.
   */
  public EpollTransport()
  {
    this(0);
  }


  /**
   * Creates a new Epoll transport.
   *
   * @param  numThreads  number of threads used by the I/O {@link EpollEventLoopGroup}
   */
  public EpollTransport(final int numThreads)
  {
    super(
      EpollSocketChannel.class,
      new EpollEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(EpollTransport.class, true, Thread.NORM_PRIORITY))));
  }
}
