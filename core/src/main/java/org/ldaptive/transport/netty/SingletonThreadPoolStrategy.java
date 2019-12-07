/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;

/**
 * Thread pool strategy that uses a single shared thread pool for all connections.
 *
 * @author  Middleware Services
 */
public class SingletonThreadPoolStrategy extends AbstractStatefulThreadPoolStrategy
{

  /** Event group used for all connections . */
  private static final EventLoopGroup SHARED_WORKER_GROUP = NettyUtils.createEventLoopGroup(
    DEFAULT_NETTY_SOCKET_TYPE,
    SingletonThreadPoolStrategy.class.getSimpleName(),
    0);


  /**
   * Creates a new singleton thread pool strategy.  Will use the default number of threads that netty allocates.
   */
  public SingletonThreadPoolStrategy()
  {
    super(DEFAULT_NETTY_SOCKET_TYPE, SingletonThreadPoolStrategy.class.getSimpleName(), SHARED_WORKER_GROUP, null);
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
