/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import com.novell.ldap.LDAPConnection;

/**
 * JLDAP provider implementation of ldap operations over SSL.
 *
 * @author  Middleware Services
 */
public class JLdapSSLConnection extends JLdapConnection
{


  /**
   * Creates a new jldap ssl connection.
   *
   * @param  conn  ldap connection
   * @param  pc  provider configuration
   */
  public JLdapSSLConnection(final LDAPConnection conn, final JLdapProviderConfig pc)
  {
    super(conn, pc);
  }
}
