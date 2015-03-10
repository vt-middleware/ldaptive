/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Decodes and encodes a boolean for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class BooleanValueTranscoder extends AbstractPrimitiveValueTranscoder<Boolean>
{


  /** Default constructor. */
  public BooleanValueTranscoder() {}


  /**
   * Creates a new boolean value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public BooleanValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  @Override
  public Boolean decodeStringValue(final String value)
  {
    return Boolean.valueOf(value);
  }


  @Override
  public Class<Boolean> getType()
  {
    return isPrimitive() ? boolean.class : Boolean.class;
  }
}
