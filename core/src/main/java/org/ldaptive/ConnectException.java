/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

/**
 * Exception that indicates a connection attempt failed.
 *
 * @author  Middleware Services
 */
public class ConnectException extends LdapException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -5935483002226156942L;


  /**
   * Creates a new connect exception.
   *
   * @param  msg  describing this exception
   */
  public ConnectException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new connect exception.
   *
   * @param  e  underlying exception
   */
  public ConnectException(final Throwable e)
  {
    super(e);
  }


  /**
   * Creates a new connect exception.
   *
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public ConnectException(final String msg, final Throwable e)
  {
    super(msg, e);
  }
}
