/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Thrown when an attempt to activate a pooled connection fails.
 *
 * @author  Middleware Services
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
