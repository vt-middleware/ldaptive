/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport.netty;

import org.ldaptive.transport.DelegateTransportFactory;
import org.ldaptive.transport.ThreadPoolConfig;
import org.ldaptive.transport.Transport;

/**
 * Netty specific implementation for creating {@link Transport}.
 *
 * @author  Middleware Services
 */
public class NettyTransportFactory implements DelegateTransportFactory
{


  @Override
  public Transport createTransport(final ThreadPoolConfig config)
  {
    Transport transport = null;
    if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.CONNECTION_CLOSE) {
      transport = new DefaultNettyTransport(
        config.getThreadPoolName(), config.getIoThreads(), config.getMessageThreads());
    } else if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.CONNECTION_FACTORY_CLOSE) {
      transport = new StatefulNettyTransport(
        config.getThreadPoolName(),
        config.getIoThreads(),
        config.getMessageThreads(),
        true);
    } else if (config.getShutdownStrategy() == ThreadPoolConfig.ShutdownStrategy.NEVER) {
      transport = new StatefulNettyTransport(
        config.getThreadPoolName(),
        config.getIoThreads(),
        config.getMessageThreads(),
        false);
    }
    if (transport == null) {
      throw new IllegalArgumentException("Unsupported shutdown strategy: " + config.getShutdownStrategy());
    }
    return transport;
  }
}
