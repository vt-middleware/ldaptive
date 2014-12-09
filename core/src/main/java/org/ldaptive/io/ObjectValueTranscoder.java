/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import org.ldaptive.LdapUtils;

/**
 * Decodes and encodes an object for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class ObjectValueTranscoder implements ValueTranscoder<Object>
{


  @Override
  public Object decodeStringValue(final String value)
  {
    return value;
  }


  @Override
  public Object decodeBinaryValue(final byte[] value)
  {
    return value;
  }


  @Override
  public String encodeStringValue(final Object value)
  {
    return value.toString();
  }


  @Override
  public byte[] encodeBinaryValue(final Object value)
  {
    return LdapUtils.utf8Encode(encodeStringValue(value));
  }


  @Override
  public Class<Object> getType()
  {
    return Object.class;
  }
}
