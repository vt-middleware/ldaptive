/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

/**
 * Thread pool strategy that creates thread pools upon instantiation and uses those pools for every connection until
 * this strategy is closed.
 *
 * @author  Middleware Services
 */
public class StatefulThreadPoolStrategy extends AbstractStatefulThreadPoolStrategy
{


  /**
   * Creates a new stateful thread pool strategy.  Will use the default number of threads that netty allocates.
   */
  public StatefulThreadPoolStrategy()
  {
    this(0);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public StatefulThreadPoolStrategy(final int ioThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, StatefulThreadPoolStrategy.class.getSimpleName(), ioThreads, -1);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public StatefulThreadPoolStrategy(final String name, final int ioThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, name, ioThreads, -1);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  type  socket type
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public StatefulThreadPoolStrategy(final NettySocketType type, final int ioThreads)
  {
    this(type, StatefulThreadPoolStrategy.class.getSimpleName(), ioThreads, -1);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public StatefulThreadPoolStrategy(final int ioThreads, final int messageThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, StatefulThreadPoolStrategy.class.getSimpleName(), ioThreads, messageThreads);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public StatefulThreadPoolStrategy(final String name, final int ioThreads, final int messageThreads)
  {
    this(DEFAULT_NETTY_SOCKET_TYPE, name, ioThreads, messageThreads);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public StatefulThreadPoolStrategy(final NettySocketType type, final String name, final int ioThreads)
  {
    this(type, name, ioThreads, -1);
  }


  /**
   * Creates a new stateful thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public StatefulThreadPoolStrategy(
    final NettySocketType type,
    final String name,
    final int ioThreads,
    final int messageThreads)
  {
    super(
      type,
      name,
      NettyUtils.createEventLoopGroup(type, name.concat("-io"), ioThreads),
      messageThreads == -1 ? null : NettyUtils.createEventLoopGroup(type, name.concat("-messages"), messageThreads));
  }
}
