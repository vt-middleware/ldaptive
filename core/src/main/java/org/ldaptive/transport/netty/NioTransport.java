/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using an {@link NioEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class NioTransport extends NettyTransport
{


  /**
   * Creates a new NIO provider.
   *
   * @param  numThreads  number of threads used by the I/O {@link NioEventLoopGroup}
   */
  public NioTransport(final int numThreads)
  {
    super(
      NioSocketChannel.class,
      new NioEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(NioTransport.class, true, Thread.NORM_PRIORITY))));
  }
}
