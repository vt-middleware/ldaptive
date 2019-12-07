/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import java.lang.reflect.Constructor;
import java.util.Map;
import io.netty.channel.ChannelOption;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.transport.Transport;

/**
 * Creates netty connections using the best fit event loop group based on the operating system. See {@link
 * io.netty.channel.epoll.Epoll#isAvailable()} and {@link io.netty.channel.kqueue.KQueue#isAvailable()}. The event loop
 * group is shutdown when the connection is closed.
 *
 * @author  Middleware Services
 */
public class NettyTransport implements Transport
{

  /** Netty thread pool strategy system property. */
  private static final String THREAD_POOL_STRATEGY_PROPERTY = "org.ldaptive.transport.netty.threadPoolStrategy";

  /** Custom thread pool strategy constructor. */
  private static final Constructor<?> THREAD_POOL_STRATEGY_CONSTRUCTOR;

  /** Thread pool strategy for managing thread pools. */
  private final ThreadPoolStrategy threadPoolStrategy;

  /** Override channel options. */
  private final Map<ChannelOption, Object> channelOptions;

  static {
    // Initialize a custom thread pool strategy if a system property is found
    final String threadPoolStrategyClass = System.getProperty(THREAD_POOL_STRATEGY_PROPERTY);
    if (threadPoolStrategyClass != null) {
      try {
        THREAD_POOL_STRATEGY_CONSTRUCTOR = Class.forName(threadPoolStrategyClass).getDeclaredConstructor();
      } catch (Exception e) {
        throw new IllegalStateException("Error instantiating " + threadPoolStrategyClass, e);
      }
    } else {
      THREAD_POOL_STRATEGY_CONSTRUCTOR = null;
    }
  }


  /**
   * Creates a new netty transport. Leverages {@link #THREAD_POOL_STRATEGY_PROPERTY} if available, otherwise creates a
   * {@link DefaultThreadPoolStrategy} with a pool size of 1.
   */
  public NettyTransport()
  {
    if (THREAD_POOL_STRATEGY_CONSTRUCTOR != null) {
      try {
        threadPoolStrategy = (ThreadPoolStrategy) THREAD_POOL_STRATEGY_CONSTRUCTOR.newInstance();
      } catch (Exception e) {
        throw new IllegalStateException(
          "Error creating new transport instance with " + THREAD_POOL_STRATEGY_CONSTRUCTOR,
          e);
      }
    } else {
      threadPoolStrategy = new DefaultThreadPoolStrategy(1);
    }
    channelOptions = null;
  }


  /**
   * Creates a new netty transport.
   *
   * @param  strategy  for creating and closing thread pools
   */
  public NettyTransport(final ThreadPoolStrategy strategy)
  {
    threadPoolStrategy = strategy;
    channelOptions = null;
  }


  /**
   * Creates a new netty transport.
   *
   * @param  strategy  for creating and closing thread pools
   * @param  options  netty channel options
   */
  public NettyTransport(final ThreadPoolStrategy strategy, final Map<ChannelOption, Object> options)
  {
    threadPoolStrategy = strategy;
    channelOptions = options;
  }


  @Override
  public Connection create(final ConnectionConfig cc)
  {
    return new NettyConnection(
      cc,
      NettyUtils.getSocketChannelType(threadPoolStrategy.getNettySocketType()),
      threadPoolStrategy.getIOThreadPool(),
      threadPoolStrategy.getMessageThreadPool(),
      channelOptions,
      threadPoolStrategy.isShutdownOnConnectionClose());
  }


  @Override
  public void close()
  {
    threadPoolStrategy.close();
  }
}
