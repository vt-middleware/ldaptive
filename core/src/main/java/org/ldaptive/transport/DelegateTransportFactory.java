/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import org.ldaptive.ConnectionFactory;

/**
 * Provides an abstraction layer for different {@link Transport} implementations used by {@link TransportFactory}.
 *
 * @author  Middleware Services
 */
public interface DelegateTransportFactory
{


  /**
   * Creates a {@link Transport} for the supplied connection factory type.
   *
   * @param  clazz  to create transport for
   *
   * @return  new transport
   */
  Transport createTransport(Class<? extends ConnectionFactory> clazz);
}
