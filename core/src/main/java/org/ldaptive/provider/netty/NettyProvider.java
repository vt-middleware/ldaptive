/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.netty;

import io.netty.channel.EventLoopGroup;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.provider.Provider;

/**
 * Creates netty connections.
 *
 * @author  Middleware Services
 */
public class NettyProvider implements Provider
{

  /** Event loop group. */
  private final EventLoopGroup workerGroup;


  /**
   * Creates a new netty provider.
   *
   * @param  group  event loop group
   */
  public NettyProvider(final EventLoopGroup group)
  {
    workerGroup = group;
  }


  @Override
  public Connection create(final ConnectionConfig cc)
  {
    return new NettyConnection(workerGroup, cc);
  }


  @Override
  public void close()
  {
    workerGroup.shutdownGracefully();
  }
}
