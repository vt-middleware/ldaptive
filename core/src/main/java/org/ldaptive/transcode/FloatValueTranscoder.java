/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes a float for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class FloatValueTranscoder extends AbstractPrimitiveValueTranscoder<Float>
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


  @Override
  public Float decodeStringValue(final String value)
  {
    return Float.valueOf(LdapUtils.assertNotNullArg(value, "Value cannot be null"));
  }


  @Override
  public Class<Float> getType()
  {
    return isPrimitive() ? float.class : Float.class;
  }
}
