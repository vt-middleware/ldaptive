/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import io.netty.channel.EventLoopGroup;

/**
 * Thread pool strategy that creates a new thread pool when the pool is requested. Pools are closed when the
 * connection using the thread pool is closed.
 *
 * @author  Middleware Services
 */
public class DefaultThreadPoolStrategy extends AbstractThreadPoolStrategy
{

  /** Number of I/O threads. */
  private final int numIoThreads;

  /** Number of message threads. */
  private final int numMessageThreads;


  /**
   * Creates a new default thread pool strategy. Will use the default number of threads that netty allocates.
   */
  public DefaultThreadPoolStrategy()
  {
    this(0);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public DefaultThreadPoolStrategy(final int ioThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, DefaultThreadPoolStrategy.class.getSimpleName(), ioThreads, -1);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public DefaultThreadPoolStrategy(final String name, final int ioThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, name, ioThreads, -1);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  type  socket type
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public DefaultThreadPoolStrategy(final NettySocketType type, final int ioThreads)
  {
    this(type, DefaultThreadPoolStrategy.class.getSimpleName(), ioThreads, -1);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public DefaultThreadPoolStrategy(final int ioThreads, final int messageThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, DefaultThreadPoolStrategy.class.getSimpleName(), ioThreads, messageThreads);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public DefaultThreadPoolStrategy(final String name, final int ioThreads, final int messageThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, name, ioThreads, messageThreads);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public DefaultThreadPoolStrategy(final NettySocketType type, final String name, final int ioThreads)
  {
    this(type, name, ioThreads, -1);
  }


  /**
   * Creates a new default thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public DefaultThreadPoolStrategy(
    final NettySocketType type,
    final String name,
    final int ioThreads,
    final int messageThreads)
  {
    super(type, name);
    numIoThreads = ioThreads;
    numMessageThreads = messageThreads;
  }


  @Override
  public EventLoopGroup getIOThreadPool()
  {
    return NettyUtils.createEventLoopGroup(getNettySocketType(), getName().concat("-io"), numIoThreads);
  }


  @Override
  public EventLoopGroup getMessageThreadPool()
  {
    if (numMessageThreads == -1) {
      return null;
    }
    return NettyUtils.createEventLoopGroup(getNettySocketType(), getName().concat("-messages"), numMessageThreads);
  }


  @Override
  public boolean isShutdownOnConnectionClose()
  {
    return true;
  }


  @Override
  public void close() {}
}
