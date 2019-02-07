/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.transcode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.ldaptive.transcode.AbstractStringValueTranscoder;

/**
 * Decodes and encodes an active directory file time value for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class FileTimeValueTranscoder extends AbstractStringValueTranscoder<ZonedDateTime>
{

  /** Number of milliseconds between standard Unix era (1/1/1970) and filetime start (1/1/1601). */
  private static final long ERA_OFFSET = 11644473600000L;

  /** File time uses 100-nanosecond intervals. For conversion purposes this is 1x10^6 / 100. */
  private static final long ONE_HUNDRED_NANOSECOND_INTERVAL = 10000L;


  @Override
  public ZonedDateTime decodeStringValue(final String value)
  {
    final Instant i = Instant.ofEpochMilli(Long.parseLong(value) / ONE_HUNDRED_NANOSECOND_INTERVAL - ERA_OFFSET);
    return ZonedDateTime.ofInstant(i, ZoneId.of("Z"));
  }


  @Override
  public String encodeStringValue(final ZonedDateTime value)
  {
    return String.valueOf((value.toInstant().toEpochMilli() + ERA_OFFSET) * ONE_HUNDRED_NANOSECOND_INTERVAL);
  }


  @Override
  public Class<ZonedDateTime> getType()
  {
    return ZonedDateTime.class;
  }
}
