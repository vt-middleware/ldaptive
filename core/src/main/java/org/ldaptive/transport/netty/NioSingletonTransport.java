/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using a single, shared {@link NioEventLoopGroup}. This event loop group uses daemon
 * threads and does not expect to be shutdown, however it can be manually shutdown using {@link #shutdown()}.
 *
 * @author  Middleware Services
 */
public class NioSingletonTransport extends NettyConnectionFactoryTransport
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = new NioEventLoopGroup(
    0,
    new ThreadPerTaskExecutor(new DefaultThreadFactory(NioSingletonTransport.class, true, Thread.NORM_PRIORITY)));


  /** Default constructor. */
  public NioSingletonTransport()
  {
    super(NioSocketChannel.class, SHARED_WORKER_GROUP);
  }


  @Override
  public void close() {}


  /**
   * Invokes {@link NettyUtils#shutdownGracefully(EventLoopGroup)} on the underlying worker group.
   */
  public static void shutdown()
  {
    NettyUtils.shutdownGracefully(SHARED_WORKER_GROUP);
  }
}
