/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jndi;

import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.provider.ControlHandler;

/**
 * JNDI request control handler.
 *
 * @author  Middleware Services
 */
public class JndiControlHandler implements ControlHandler<javax.naming.ldap.Control>
{


  @Override
  public Class<javax.naming.ldap.Control> getControlType()
  {
    return javax.naming.ldap.Control.class;
  }


  @Override
  public String getOID(final javax.naming.ldap.Control control)
  {
    return control.getID();
  }


  @Override
  public javax.naming.ldap.Control handleRequest(final RequestControl requestControl)
  {
    return
      new javax.naming.ldap.BasicControl(
        requestControl.getOID(),
        requestControl.getCriticality(),
        requestControl.encode());
  }


  @Override
  public ResponseControl handleResponse(final javax.naming.ldap.Control responseControl)
  {
    return
      ControlFactory.createResponseControl(
        responseControl.getID(),
        responseControl.isCritical(),
        responseControl.getEncodedValue());
  }
}
