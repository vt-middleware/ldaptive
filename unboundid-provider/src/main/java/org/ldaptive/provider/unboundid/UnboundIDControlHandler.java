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
package org.ldaptive.provider.unboundid;

import com.unboundid.asn1.ASN1OctetString;
import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.provider.ControlHandler;

/**
 * Unbound ID control handler.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class UnboundIDControlHandler
  implements ControlHandler<com.unboundid.ldap.sdk.Control>
{


  /** {@inheritDoc} */
  @Override
  public String getOID(final com.unboundid.ldap.sdk.Control control)
  {
    return control.getOID();
  }


  /** {@inheritDoc} */
  @Override
  public com.unboundid.ldap.sdk.Control handleRequest(
    final RequestControl requestControl)
  {
    final byte[] value = requestControl.encode();
    if (value == null) {
      return
        new com.unboundid.ldap.sdk.Control(
          requestControl.getOID(),
          requestControl.getCriticality());
    } else {
      return
        new com.unboundid.ldap.sdk.Control(
          requestControl.getOID(),
          requestControl.getCriticality(),
          new ASN1OctetString(value));
    }
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl handleResponse(
    final com.unboundid.ldap.sdk.Control responseControl)
  {
    return
      ControlFactory.createResponseControl(
        responseControl.getOID(),
        responseControl.isCritical(),
        responseControl.getValue().getValue());
  }
}
