/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes a character array for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class CharArrayValueTranscoder extends AbstractStringValueTranscoder<char[]>
{


  @Override
  public char[] decodeStringValue(final String value)
  {
    return LdapUtils.assertNotNullArg(value, "Value cannot be null").toCharArray();
  }


  @Override
  public String encodeStringValue(final char[] value)
  {
    return String.valueOf(LdapUtils.assertNotNullArg(value, "Value cannot be null"));
  }


  @Override
  public Class<char[]> getType()
  {
    return char[].class;
  }
}
