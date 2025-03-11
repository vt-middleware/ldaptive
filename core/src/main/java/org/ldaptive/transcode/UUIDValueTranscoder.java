/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.util.UUID;
import org.ldaptive.LdapUtils;

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
    return UUID.fromString(LdapUtils.assertNotNullArg(value, "Value cannot be null"));
  }


  @Override
  public String encodeStringValue(final UUID value)
  {
    return LdapUtils.assertNotNullArg(value, "Value cannot be null").toString();
  }


  @Override
  public Class<UUID> getType()
  {
    return UUID.class;
  }
}
