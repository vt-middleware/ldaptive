/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using a single, shared {@link NioEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class SharedNettyProvider extends NettyProvider
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = new NioEventLoopGroup(
    0,
    new ThreadPerTaskExecutor(new DefaultThreadFactory(SharedNettyProvider.class, true, Thread.NORM_PRIORITY)));


  /** Default constructor. */
  public SharedNettyProvider()
  {
    super(SHARED_WORKER_GROUP);
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
