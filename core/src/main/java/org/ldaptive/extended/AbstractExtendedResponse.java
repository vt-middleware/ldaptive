/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

/**
 * Provides common implementation for extended responses.
 *
 * @param  <T>  type of response value
 *
 * @author  Middleware Services
 */
public abstract class AbstractExtendedResponse<T> implements ExtendedResponse<T>
{

  /** Response value. */
  private T value;


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
