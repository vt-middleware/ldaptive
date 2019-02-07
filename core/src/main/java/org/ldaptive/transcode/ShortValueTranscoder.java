/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

/**
 * Decodes and encodes a short for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class ShortValueTranscoder extends AbstractPrimitiveValueTranscoder<Short>
{


  /** Default constructor. */
  public ShortValueTranscoder() {}


  /**
   * Creates a new short value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public ShortValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  @Override
  public Short decodeStringValue(final String value)
  {
    return Short.valueOf(value);
  }


  @Override
  public Class<Short> getType()
  {
    return isPrimitive() ? short.class : Short.class;
  }
}
