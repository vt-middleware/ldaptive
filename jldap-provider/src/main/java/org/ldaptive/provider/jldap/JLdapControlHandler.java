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

import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.provider.ControlHandler;

/**
 * JLdap control handler.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class JLdapControlHandler
  implements ControlHandler<com.novell.ldap.LDAPControl>
{


  /** {@inheritDoc} */
  @Override
  public String getOID(final com.novell.ldap.LDAPControl control)
  {
    return control.getID();
  }


  /** {@inheritDoc} */
  @Override
  public com.novell.ldap.LDAPControl handleRequest(
    final RequestControl requestControl)
  {
    return
      new com.novell.ldap.LDAPControl(
        requestControl.getOID(),
        requestControl.getCriticality(),
        requestControl.encode());
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl handleResponse(
    final com.novell.ldap.LDAPControl responseControl)
  {
    return
      ControlFactory.createResponseControl(
        responseControl.getID(),
        responseControl.isCritical(),
        responseControl.getValue());
  }
}
