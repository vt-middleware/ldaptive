/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;

/**
 * Provides an abstraction layer for different {@link Connection} implementations.
 *
 * @author  Middleware Services
 */
public interface Transport
{


  /**
   * Create a connection object. Implementations should not open a TCP socket in this method.
   *
   * @param  cc  connection configuration
   *
   * @return  connection
   */
  Connection create(ConnectionConfig cc);


  /**
   * Free any resources associated with this transport. This method is invoked by the connection factory using this
   * transport.
   */
  void close();


  /**
   * Force shutdown of this transport. This method is only needed in cases where the connection factory is configured
   * not to close the transport. See {@link ThreadPoolConfig#setShutdownStrategy(ThreadPoolConfig.ShutdownStrategy)}.
   */
  void shutdown();
}
