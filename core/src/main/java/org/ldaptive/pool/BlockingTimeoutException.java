/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Thrown when a blocking operation times out. See {@link
 * ConnectionPool#getConnection()}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class BlockingTimeoutException extends PoolException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = 6013765020562222482L;


  /**
   * Creates a new blocking timeout exception.
   *
   * @param  msg  describing this exception
   */
  public BlockingTimeoutException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new blocking timeout exception.
   *
   * @param  e  pooling specific exception
   */
  public BlockingTimeoutException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new blocking timeout exception.
   *
   * @param  msg  describing this exception
   * @param  e  pooling specific exception
   */
  public BlockingTimeoutException(final String msg, final Exception e)
  {
    super(msg, e);
  }
}
