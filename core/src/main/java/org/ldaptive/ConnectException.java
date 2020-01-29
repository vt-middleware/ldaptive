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
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   */
  public ConnectException(final ResultCode code, final String msg)
  {
    super(code, msg);
  }


  /**
   * Creates a new connect exception.
   *
   * @param  code result code describing this exception
   * @param  e  underlying exception
   */
  public ConnectException(final ResultCode code, final Throwable e)
  {
    super(code, e);
  }


  /**
   * Creates a new connect exception.
   *
   * @param  code result code describing this exception
   * @param  msg  describing this exception
   * @param  e  underlying exception
   */
  public ConnectException(final ResultCode code, final String msg, final Throwable e)
  {
    super(code, msg, e);
  }
}
