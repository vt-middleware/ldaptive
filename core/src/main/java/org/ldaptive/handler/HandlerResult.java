/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

/**
 * Handler result data.
 *
 * @param  <T>  type of result
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class HandlerResult<T>
{

  /** Result produced by a handler. */
  private final T result;

  /** Whether the operation should be aborted. */
  private final boolean abort;


  /**
   * Creates a new handler result.
   *
   * @param  t  produced by a handler
   */
  public HandlerResult(final T t)
  {
    result = t;
    abort = false;
  }


  /**
   * Creates a new handler result.
   *
   * @param  t  produced by a handler
   * @param  b  whether the operation should be aborted
   */
  public HandlerResult(final T t, final boolean b)
  {
    result = t;
    abort = b;
  }


  /**
   * Returns the result produced by a handler.
   *
   * @return  result
   */
  public T getResult()
  {
    return result;
  }


  /**
   * Returns whether the operation should be aborted.
   *
   * @return  whether the operation should be aborted
   */
  public boolean getAbort()
  {
    return abort;
  }
}
