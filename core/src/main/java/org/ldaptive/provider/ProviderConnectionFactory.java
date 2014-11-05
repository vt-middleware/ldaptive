/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.LdapException;

/**
 * Provides an interface for creating provider connections.
 *
 * @param  <T>  type of provider config for this connection factory
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
