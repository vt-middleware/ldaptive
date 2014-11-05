/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Decodes and encodes a long for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class LongValueTranscoder extends AbstractPrimitiveValueTranscoder<Long>
{


  /** Default constructor. */
  public LongValueTranscoder() {}


  /**
   * Creates a new long value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public LongValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  /** {@inheritDoc} */
  @Override
  public Long decodeStringValue(final String value)
  {
    return Long.valueOf(value);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Long> getType()
  {
    return isPrimitive() ? long.class : Long.class;
  }
}
