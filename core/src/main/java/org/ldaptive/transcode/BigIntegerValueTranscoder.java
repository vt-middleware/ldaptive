/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.math.BigInteger;

/**
 * Decodes and encodes a big integer for use in an ldap attribute value. This implementation assumes a base 10 number.
 *
 * @author  Middleware Services
 */
public class BigIntegerValueTranscoder extends AbstractStringValueTranscoder<BigInteger>
{


  @Override
  public BigInteger decodeStringValue(final String value)
  {
    return new BigInteger(value);
  }


  @Override
  public String encodeStringValue(final BigInteger value)
  {
    return value.toString();
  }


  @Override
  public Class<BigInteger> getType()
  {
    return BigInteger.class;
  }
}
