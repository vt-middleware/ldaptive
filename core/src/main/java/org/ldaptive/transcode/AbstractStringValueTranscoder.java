/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import org.ldaptive.LdapUtils;

/**
 * Value transcoder which decodes and encodes to a String and therefore the binary methods simply delegate to the string
 * methods.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 */
public abstract class AbstractStringValueTranscoder<T> implements ValueTranscoder<T>
{


  @Override
  public T decodeBinaryValue(final byte[] value)
  {
    return decodeStringValue(LdapUtils.utf8Encode(value));
  }


  @Override
  public byte[] encodeBinaryValue(final T value)
  {
    return LdapUtils.utf8Encode(encodeStringValue(value));
  }
}
