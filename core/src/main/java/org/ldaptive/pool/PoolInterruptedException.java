/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Thrown when a pool thread is unexpectedly interrupted while blocking.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class PoolInterruptedException extends PoolException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -1427225156311025280L;


  /**
   * Creates a new pool interrupted exception.
   *
   * @param  msg  describing this exception
   */
  public PoolInterruptedException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new pool interrupted exception.
   *
   * @param  e  pooling specific exception
   */
  public PoolInterruptedException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new pool interrupted exception.
   *
   * @param  msg  describing this exception
   * @param  e  pooling specific exception
   */
  public PoolInterruptedException(final String msg, final Exception e)
  {
    super(msg, e);
  }
}
