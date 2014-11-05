/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Decodes and encodes a byte array for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class ByteArrayValueTranscoder
  extends AbstractBinaryValueTranscoder<byte[]>
{


  /** {@inheritDoc} */
  @Override
  public byte[] decodeBinaryValue(final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public byte[] encodeBinaryValue(final byte[] value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public Class<byte[]> getType()
  {
    return byte[].class;
  }
}
