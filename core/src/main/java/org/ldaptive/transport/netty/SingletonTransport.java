/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;

/**
 * Creates netty connections using a single, shared {@link EventLoopGroup} using the best fit event loop group based on
 * the operating system. See {@link io.netty.channel.epoll.Epoll#isAvailable()} and {@link
 * io.netty.channel.kqueue.KQueue#isAvailable()}. This event loop group uses daemon threads and does not expect to be
 * shutdown, however it can be manually shutdown using {@link #shutdown()}.
 *
 * @author  Middleware Services
 */
public class SingletonTransport extends NettyConnectionFactoryTransport
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = NettyUtils.createDefaultEventLoopGroup(
    SingletonTransport.class.getSimpleName(),
    0);


  /** Default constructor. */
  public SingletonTransport()
  {
    super(NettyUtils.getDefaultSocketChannelType(), SHARED_WORKER_GROUP);
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
