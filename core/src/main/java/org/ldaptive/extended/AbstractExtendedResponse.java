/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

/**
 * Provides common implementation for extended responses.
 *
 * @param  <T>  type of response value
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractExtendedResponse<T> implements ExtendedResponse<T>
{

  /** Response value. */
  private T value;


  /** {@inheritDoc} */
  @Override
  public T getValue()
  {
    return value;
  }


  /**
   * Sets the response value for this extended operation.
   *
   * @param  t  response value
   */
  protected void setValue(final T t)
  {
    value = t;
  }
}
