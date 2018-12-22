/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

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
}
