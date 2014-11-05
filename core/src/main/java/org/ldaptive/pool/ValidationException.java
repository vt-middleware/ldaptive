/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

/**
 * Thrown when an attempt to validate a pooled object fails.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class ValidationException extends PoolException
{

  /** serialVersionUID. */
  private static final long serialVersionUID = -5043560632396467010L;


  /**
   * Creates a new validation exception.
   *
   * @param  msg  describing this exception
   */
  public ValidationException(final String msg)
  {
    super(msg);
  }


  /**
   * Creates a new validation exception.
   *
   * @param  e  pooling specific exception
   */
  public ValidationException(final Exception e)
  {
    super(e);
  }


  /**
   * Creates a new validation exception.
   *
   * @param  msg  describing this exception
   * @param  e  pooling specific exception
   */
  public ValidationException(final String msg, final Exception e)
  {
    super(msg, e);
  }
}
