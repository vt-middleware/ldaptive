/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;

/**
 * Interface for thread pool strategies.
 *
 * @author  Middleware Services
 */
public interface ThreadPoolStrategy
{


  /**
   * Returns the thread pool used for I/O events. Cannot be null.
   *
   * @return  I/O thread pool
   */
  EventLoopGroup getIOThreadPool();


  /**
   * Returns the thread pool used for message events. Can be null to indicate that the I/O pool should be used for
   * message events.
   *
   * @return  message thread pool or null
   */
  EventLoopGroup getMessageThreadPool();


  /**
   * Socket type used by this thread pool strategy.
   *
   * @return  socket type
   */
  NettySocketType getNettySocketType();


  /**
   * Whether to shutdown the thread pools on connection close. See {@link org.ldaptive.Connection#close()}.
   *
   * @return  whether to shutdown thread pools on connection close
   */
  boolean isShutdownOnConnectionClose();


  /** Free any resources associated with this strategy. */
  void close();
}
