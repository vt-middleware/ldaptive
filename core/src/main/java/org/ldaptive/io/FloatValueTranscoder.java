/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Decodes and encodes a float for use in an ldap attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 2886 $ $Date: 2014-02-26 12:21:59 -0500 (Wed, 26 Feb 2014) $
 */
public class FloatValueTranscoder
  extends AbstractPrimitiveValueTranscoder<Float>
{


  /** Default constructor. */
  public FloatValueTranscoder() {}


  /**
   * Creates a new float value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public FloatValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  /** {@inheritDoc} */
  @Override
  public Float decodeStringValue(final String value)
  {
    return Float.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Float> getType()
  {
    return isPrimitive() ? float.class : Float.class;
  }
}
