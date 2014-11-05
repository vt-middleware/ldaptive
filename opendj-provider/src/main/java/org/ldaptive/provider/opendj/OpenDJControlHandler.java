/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.opendj;

import org.forgerock.opendj.ldap.ByteStringBuilder;
import org.forgerock.opendj.ldap.controls.GenericControl;
import org.ldaptive.control.ControlFactory;
import org.ldaptive.control.RequestControl;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.provider.ControlHandler;

/**
 * OpenDJ control handler.
 *
 * @author  Middleware Services
 */
public class OpenDJControlHandler
  implements ControlHandler<org.forgerock.opendj.ldap.controls.Control>
{


  /** {@inheritDoc} */
  @Override
  public String getOID(final org.forgerock.opendj.ldap.controls.Control control)
  {
    return control.getOID();
  }


  /** {@inheritDoc} */
  @Override
  public org.forgerock.opendj.ldap.controls.Control handleRequest(
    final RequestControl requestControl)
  {
    final byte[] value = requestControl.encode();
    if (value == null) {
      return
        GenericControl.newControl(
          requestControl.getOID(),
          requestControl.getCriticality());
    } else {
      final ByteStringBuilder builder = new ByteStringBuilder(value.length);
      builder.append(value);
      return
        GenericControl.newControl(
          requestControl.getOID(),
          requestControl.getCriticality(),
          builder.toByteString());
    }
  }


  /** {@inheritDoc} */
  @Override
  public ResponseControl handleResponse(
    final org.forgerock.opendj.ldap.controls.Control responseControl)
  {
    return
      ControlFactory.createResponseControl(
        responseControl.getOID(),
        responseControl.isCritical(),
        responseControl.getValue().toByteArray());
  }
}
