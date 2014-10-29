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
package org.ldaptive.auth;

import org.ldaptive.LdapException;

/**
 * Provides an interface for LDAP authentication implementations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface AuthenticationHandler
{


  /**
   * Perform an ldap authentication.
   *
   * @param  criteria  to perform the authentication with
   *
   * @return  authentication handler response
   *
   * @throws  LdapException  if ldap operation fails
   */
  AuthenticationHandlerResponse authenticate(AuthenticationCriteria criteria)
    throws LdapException;
}
