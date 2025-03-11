/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes a double for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class DoubleValueTranscoder extends AbstractPrimitiveValueTranscoder<Double>
{


  /** Default constructor. */
  public DoubleValueTranscoder() {}


  /**
   * Creates a new double value transcoder.
   *
   * @param  b  whether this transcoder is operating on a primitive
   */
  public DoubleValueTranscoder(final boolean b)
  {
    setPrimitive(b);
  }


  @Override
  public Double decodeStringValue(final String value)
  {
    return Double.valueOf(LdapUtils.assertNotNullArg(value, "Value cannot be null"));
  }


  @Override
  public Class<Double> getType()
  {
    return isPrimitive() ? double.class : Double.class;
  }
}
