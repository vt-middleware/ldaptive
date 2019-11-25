/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;

/**
 * Creates netty connections using an {@link KQueueEventLoopGroup}.
 *
 * @author  Middleware Services
 */
public class KQueueTransport extends NettyTransport
{


  /**
   * Creates a new KQueue transport.
   */
  public KQueueTransport()
  {
    this(0);
  }


  /**
   * Creates a new KQueue transport.
   *
   * @param  numThreads  number of threads used by the I/O {@link KQueueEventLoopGroup}
   */
  public KQueueTransport(final int numThreads)
  {
    super(
      KQueueSocketChannel.class,
      new KQueueEventLoopGroup(
        numThreads,
        new ThreadPerTaskExecutor(new DefaultThreadFactory(KQueueTransport.class, true, Thread.NORM_PRIORITY))));
  }
}
