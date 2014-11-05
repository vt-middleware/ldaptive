/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.control.ResponseControl;

/**
 * Interface for ldap response messages.
 *
 * @author  Middleware Services
 */
public interface ResponseMessage extends Message<ResponseControl>
{


  /**
   * Returns the first response control with the supplied OID for this response
   * message.
   *
   * @param  oid  of the response control to return
   *
   * @return  response control or null if control could not be found
   */
  ResponseControl getControl(String oid);


  /**
   * Returns the message ID for this response message.
   *
   * @return  message id
   */
  int getMessageId();
}
