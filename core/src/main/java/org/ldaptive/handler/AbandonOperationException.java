/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.LdapException;

/**
 * Exception thrown by a handler that indicates the operation should be abandoned.
 *
 * @author  Middleware Services
 */
public class AbandonOperationException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -8097328059259999823L;


  /**
   * Creates a new abandon exception.
   *
   * @param  msg  describing this exception
   */
  public AbandonOperationException(final String msg)
  {
    super(msg);
  }
}
