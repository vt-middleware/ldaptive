/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;

/**
 * Provides an abstraction layer for different {@link ProviderConnection} implementations.
 *
 * @author  Middleware Services
 */
public interface Provider
{


  /**
   * Create a connection object. Implementations should not open a TCP socket in this method.
   *
   * @param  factory  Connection factory used to produce connections.
   *
   * @return  Provider connection instance.
   */
  Connection create(ConnectionFactory factory);


  /** Free any resources associated with this provider. */
  default void close() {}
}
