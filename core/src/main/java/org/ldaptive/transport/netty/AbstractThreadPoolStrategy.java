/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for thread pool strategy implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractThreadPoolStrategy implements ThreadPoolStrategy
{

  /** Custom thread pool strategy constructor. */
  protected static final NettySocketType DEFAULT_NETTY_SOCKET_TYPE;

  /** Netty socket type system property. */
  private static final String NETTY_SOCKET_TYPE_PROPERTY = "org.ldaptive.transport.netty.socketType";

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Netty socket type. */
  private final NettySocketType socketType;

  /** Name of the thread pool. */
  private final String poolName;

  static {
    // Initialize a default netty socket type if a system property is found
    final String socketTypeEnum = System.getProperty(NETTY_SOCKET_TYPE_PROPERTY);
    if (socketTypeEnum != null) {
      try {
        DEFAULT_NETTY_SOCKET_TYPE = NettySocketType.valueOf(socketTypeEnum);
      } catch (Exception e) {
        throw new IllegalStateException("Unknown netty socket type: " + socketTypeEnum, e);
      }
    } else {
      DEFAULT_NETTY_SOCKET_TYPE = NettySocketType.DEFAULT;
    }
  }


  /**
   * Creates a new abstract thread pool strategy.
   *
   * @param  type  socket type
   * @param  name  of the thread pool
   */
  public AbstractThreadPoolStrategy(final NettySocketType type, final String name)
  {
    socketType = type;
    poolName = name;
  }


  @Override
  public NettySocketType getNettySocketType()
  {
    return socketType;
  }


  /**
   * Returns the name of this thread pool strategy.
   *
   * @return  thread pool name
   */
  public String getName()
  {
    return poolName;
  }
}
