/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.util.UUID;

/**
 * Decodes and encodes a UUID for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class UUIDValueTranscoder extends AbstractStringValueTranscoder<UUID>
{


  @Override
  public UUID decodeStringValue(final String value)
  {
    return UUID.fromString(value);
  }


  @Override
  public String encodeStringValue(final UUID value)
  {
    return value.toString();
  }


  @Override
  public Class<UUID> getType()
  {
    return UUID.class;
  }
}
