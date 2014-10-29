/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.provider.jldap;

import com.novell.ldap.LDAPConnection;

/**
 * JLDAP provider implementation of ldap operations over SSL.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class JLdapSSLConnection extends JLdapConnection
{


  /**
   * Creates a new jldap ssl connection.
   *
   * @param  conn  ldap connection
   * @param  pc  provider configuration
   */
  public JLdapSSLConnection(
    final LDAPConnection conn,
    final JLdapProviderConfig pc)
  {
    super(conn, pc);
  }
}
