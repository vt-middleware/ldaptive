/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.util.UUID;

/**
 * Decodes and encodes a UUID for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class UUIDValueTranscoder extends AbstractStringValueTranscoder<UUID>
{


  /** {@inheritDoc} */
  @Override
  public UUID decodeStringValue(final String value)
  {
    return UUID.fromString(value);
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final UUID value)
  {
    return value.toString();
  }


  /** {@inheritDoc} */
  @Override
  public Class<UUID> getType()
  {
    return UUID.class;
  }
}
