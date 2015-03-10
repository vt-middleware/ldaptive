/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Base class for primitive value transcoders.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 */
public abstract class AbstractPrimitiveValueTranscoder<T> extends AbstractStringValueTranscoder<T>
{

  /** Whether this transcoder operates on a primitive or an object. */
  private boolean primitive;


  /**
   * Returns whether this transcoder operates on a primitive value.
   *
   * @return  whether this transcoder operates on a primitive value
   */
  public boolean isPrimitive()
  {
    return primitive;
  }


  /**
   * Sets whether this transcoder operates on a primitive value.
   *
   * @param  b  whether this transcoder operates on a primitive value
   */
  public void setPrimitive(final boolean b)
  {
    primitive = b;
  }


  @Override
  public String encodeStringValue(final T value)
  {
    return value.toString();
  }
}
