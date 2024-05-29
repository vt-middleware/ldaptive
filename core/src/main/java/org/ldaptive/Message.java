/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.List;
import org.ldaptive.control.ResponseControl;

/**
 * LDAP protocol response.
 *
 * @author  Middleware Services
 */
public interface Message
{

  /**
   * Returns the ID for this message.
   *
   * @return  message ID
   */
  int getMessageID();


  /**
   * Returns the response controls for this message.
   *
   * @return  response controls
   */
  ResponseControl[] getControls();


  /**
   * Returns the first response control with the supplied OID.
   *
   * @param  oid  of the response control to return
   *
   * @return  response control or null if control could not be found
   */
  default ResponseControl getControl(final String oid)
  {
    if (getControls() != null) {
      for (ResponseControl c : getControls()) {
        if (c != null && c.getOID().equals(oid)) {
          return c;
        }
      }
    }
    return null;
  }

  /** Message builder interface. */
  interface Builder<T extends Message>
  {
    void messageID(int messageID);

    void controls(ResponseControl... controls);

    void controls(List<ResponseControl> controls);

    T build();
  }
}
