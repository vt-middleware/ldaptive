/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;

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
   * @param  cc  connection configuration
   *
   * @return  provider connection
   */
  Connection create(ConnectionConfig cc);


  /** Free any resources associated with this provider. */
  default void close() {}
}
