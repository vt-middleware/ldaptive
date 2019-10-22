/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using a single, shared {@link KQueueEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class SharedKQueueTransport extends NettyTransport
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = new KQueueEventLoopGroup(
    0,
    new ThreadPerTaskExecutor(new DefaultThreadFactory(SharedKQueueTransport.class, true, Thread.NORM_PRIORITY)));


  /** Default constructor. */
  public SharedKQueueTransport()
  {
    super(KQueueSocketChannel.class, SHARED_WORKER_GROUP);
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
