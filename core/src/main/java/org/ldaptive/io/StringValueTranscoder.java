/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

/**
 * Decodes and encodes a string for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class StringValueTranscoder extends AbstractStringValueTranscoder<String>
{


  /** {@inheritDoc} */
  @Override
  public String decodeStringValue(final String value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final String value)
  {
    return value;
  }


  /** {@inheritDoc} */
  @Override
  public Class<String> getType()
  {
    return String.class;
  }
}
