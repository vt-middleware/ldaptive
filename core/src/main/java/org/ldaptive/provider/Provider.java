/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.ConnectionConfig;

/**
 * Provides access to a provider specific connection factory.
 *
 * @param  <T>  type of provider config for this provider
 *
 * @author  Middleware Services
 */
public interface Provider<T extends ProviderConfig>
{


  /**
   * Returns the provider configuration.
   *
   * @return  provider configuration
   */
  T getProviderConfig();


  /**
   * Sets the provider configuration.
   *
   * @param  pc  provider configuration
   */
  void setProviderConfig(T pc);


  /**
   * Returns the connection factory for this provider.
   *
   * @param  cc  connection configuration
   *
   * @return  connection factory
   */
  ProviderConnectionFactory<T> getConnectionFactory(ConnectionConfig cc);


  /**
   * Creates a new instance of this provider.
   *
   * @return  new instance of this provider
   */
  Provider<T> newInstance();
}
