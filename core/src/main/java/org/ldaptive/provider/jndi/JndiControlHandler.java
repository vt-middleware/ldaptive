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
package org.ldaptive.provider.jndi;

import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.provider.ControlHandler;

/**
 * JNDI request control handler.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class JndiControlHandler
  implements ControlHandler<javax.naming.ldap.Control>
{


  /** {@inheritDoc} */
  @Override
  public String getOID(final javax.naming.ldap.Control control)
  {
    return control.getID();
  }


  /** {@inheritDoc} */
  @Override
  public javax.naming.ldap.Control handleRequest(
    final RequestControl requestControl)
  {
    return
      new javax.naming.ldap.BasicControl(
        requestControl.getOID(),
        requestControl.getCriticality(),
        requestControl.encode());
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl handleResponse(
    final javax.naming.ldap.Control responseControl)
  {
    return
      ControlFactory.createResponseControl(
        responseControl.getID(),
        responseControl.isCritical(),
        responseControl.getEncodedValue());
  }
}
