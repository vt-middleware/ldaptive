/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using a single, shared {@link EpollEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class SharedEpollTransport extends NettyTransport
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = new EpollEventLoopGroup(
    0,
    new ThreadPerTaskExecutor(new DefaultThreadFactory(SharedEpollTransport.class, true, Thread.NORM_PRIORITY)));


  /** Default constructor. */
  public SharedEpollTransport()
  {
    super(EpollSocketChannel.class, SHARED_WORKER_GROUP);
  }


  @Override
  public void close() {}


  /**
   * Invokes {@link EventLoopGroup#shutdownGracefully()} on the underlying worker group.
   */
  public static void shutdown()
  {
    SHARED_WORKER_GROUP.shutdownGracefully();
  }
}
