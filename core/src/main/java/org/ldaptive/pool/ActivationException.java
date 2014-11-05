/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Thrown when an attempt to activate a pooled object fails.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ActivationException extends PoolException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = 5547712224386623996L;


  /**
   * Creates a new activation exception.
   *
   * @param  msg  describing this exception
   */
  public ActivationException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new activation exception.
   *
   * @param  e  pooling specific exception
   */
  public ActivationException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new activation exception.
   *
   * @param  msg  describing this exception
   * @param  e  pooling specific exception
   */
  public ActivationException(final String msg, final Exception e)
  {
    super(msg, e);
  }
}
