/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;

/**
 * Provides an abstraction layer for different {@link TransportConnection} implementations.
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


  /** Free any resources associated with this transport. */
  void close();
}
