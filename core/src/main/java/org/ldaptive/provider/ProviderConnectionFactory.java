/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.LdapException;

/**
 * Provides an interface for creating provider connections.
 *
 * @param  <T>  type of provider config for this connection factory
 *
 * @author  Middleware Services
 */
public interface ProviderConnectionFactory<T extends ProviderConfig>
{


  /**
   * Returns the provider configuration.
   *
   * @return  provider configuration
   */
  T getProviderConfig();


  /**
   * Create a connection to an LDAP.
   *
   * @return  provider connection
   *
   * @throws  LdapException  if an LDAP error occurs
   */
  ProviderConnection create()
    throws LdapException;
}
