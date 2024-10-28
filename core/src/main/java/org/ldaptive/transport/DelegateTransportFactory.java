/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

/**
 * Provides an abstraction layer for different {@link Transport} implementations used by {@link TransportFactory}.
 *
 * @author  Middleware Services
 */
public interface DelegateTransportFactory
{


  /**
   * Creates a {@link Transport} for the supplied transport configuration.
   *
   * @param  config  transport configuration
   *
   * @return  new transport
   */
  Transport createTransport(ThreadPoolConfig config);
}
