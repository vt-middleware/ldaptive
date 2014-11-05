/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import org.ldaptive.LdapUtils;

/**
 * Value transcoder which decodes and encodes to a byte array and therefore the
 * string methods simply delegate to the binary methods.
 *
 * @param  <T>  type of object to transcode
 *
 * @author  Middleware Services
 * @version  $Revision: 2998 $ $Date: 2014-06-11 13:28:09 -0400 (Wed, 11 Jun 2014) $
 */
public abstract class AbstractBinaryValueTranscoder<T>
  implements ValueTranscoder<T>
{


  /** {@inheritDoc} */
  @Override
  public T decodeStringValue(final String value)
  {
    return decodeBinaryValue(LdapUtils.utf8Encode(value));
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final T value)
  {
    return LdapUtils.utf8Encode(encodeBinaryValue(value));
  }
}
