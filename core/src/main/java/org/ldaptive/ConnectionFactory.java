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
package org.ldaptive;

/**
 * Factory for creating connections.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface ConnectionFactory
{


  /**
   * Creates a new connection.
   *
   * @return  connection
   *
   * @throws  LdapException  if a connection cannot be returned
   */
  Connection getConnection()
    throws LdapException;
}
