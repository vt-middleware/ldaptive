/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import org.ldaptive.LdapUtils;

/**
 * Value transcoder which decodes and encodes to a byte array and therefore the string methods simply delegate to the
 * binary methods.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 */
public abstract class AbstractBinaryValueTranscoder<T> implements ValueTranscoder<T>
{


  @Override
  public T decodeStringValue(final String value)
  {
    return decodeBinaryValue(LdapUtils.utf8Encode(value, false));
  }


  @Override
  public String encodeStringValue(final T value)
  {
    return LdapUtils.utf8Encode(encodeBinaryValue(value));
  }
}
