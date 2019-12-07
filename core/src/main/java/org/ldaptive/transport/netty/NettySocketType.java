/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;

/**
 * Enum to define netty socket types.
 *
 * @author  Middleware Services
 */
public enum NettySocketType {

  /** Attempts to determine the best supported socket type, which will be one of the other listed types. */
  DEFAULT,

  /** Java NIO socket type. */
  NIO,

  /** Epoll socket type for Linux. */
  EPOLL,

  /** KQueue socket type for Mac. */
  KQUEUE;


  /**
   * Returns the platform specific netty socket type. See {@link Epoll#isAvailable()} and {@link KQueue#isAvailable()}.
   *
   * @return  one of {@link #NIO}, {@link #EPOLL} or {@link #KQUEUE}
   */
  public static NettySocketType getDefaultSocketType()
  {
    if (Epoll.isAvailable()) {
      return NettySocketType.EPOLL;
    } else if (KQueue.isAvailable()) {
      return NettySocketType.KQUEUE;
    } else {
      return NettySocketType.NIO;
    }
  }
}
