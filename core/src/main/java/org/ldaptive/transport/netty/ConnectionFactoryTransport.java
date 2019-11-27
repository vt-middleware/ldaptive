/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.util.Map;
import io.netty.channel.ChannelOption;

/**
 * Creates netty connections using the best fit event loop group based on the operating system. See {@link
 * io.netty.channel.epoll.Epoll#isAvailable()} and {@link io.netty.channel.kqueue.KQueue#isAvailable()}. The event loop
 * group is shutdown when the connection factory is closed.
 *
 * @author  Middleware Services
 */
public class ConnectionFactoryTransport extends NettyConnectionFactoryTransport
{


  /**
   * Creates a new connection factory transport.
   */
  public ConnectionFactoryTransport()
  {
    this(0);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public ConnectionFactoryTransport(final int ioThreads)
  {
    this(ConnectionFactoryTransport.class.getSimpleName(), ioThreads);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  name  to assign the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   */
  public ConnectionFactoryTransport(final String name, final int ioThreads)
  {
    this(name, ioThreads, null);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  name  to assign the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  options  netty channel options
   */
  public ConnectionFactoryTransport(final String name, final int ioThreads, final Map<ChannelOption, Object> options)
  {
    super(
      NettyUtils.getDefaultSocketChannelType(),
      NettyUtils.createDefaultEventLoopGroup(name, ioThreads),
      null,
      options);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public ConnectionFactoryTransport(final int ioThreads, final int messageThreads)
  {
    this(ConnectionFactoryTransport.class.getSimpleName(), ioThreads, messageThreads);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  name  to assign the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   */
  public ConnectionFactoryTransport(final String name, final int ioThreads, final int messageThreads)
  {
    this(name, ioThreads, messageThreads, null);
  }


  /**
   * Creates a new connection factory transport.
   *
   * @param  name  to assign the thread pool
   * @param  ioThreads  number of threads used for I/O in the event loop group
   * @param  messageThreads  number of threads for LDAP message handling in the event loop group
   * @param  options  netty channel options
   */
  public ConnectionFactoryTransport(
    final String name,
    final int ioThreads,
    final int messageThreads,
    final Map<ChannelOption, Object> options)
  {
    super(
      NettyUtils.getDefaultSocketChannelType(),
      NettyUtils.createDefaultEventLoopGroup(name.concat("-io"), ioThreads),
      NettyUtils.createDefaultEventLoopGroup(name.concat("-messages"), messageThreads),
      options);
  }
}
