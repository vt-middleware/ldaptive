/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.io;

import org.ldaptive.io.AbstractStringValueTranscoder;

/**
 * Decodes and encodes an active directory delta time value for use in an ldap attribute value.
 *
 * @author  Middleware Services
 */
public class DeltaTimeValueTranscoder extends AbstractStringValueTranscoder<Long>
{

  /** Delta time uses 100-nanosecond intervals. For conversion purposes this is 1x10^6 / 100. */
  private static final long ONE_HUNDRED_NANOSECOND_INTERVAL = 10000L;


  @Override
  public Long decodeStringValue(final String value)
  {
    return -Long.parseLong(value) / ONE_HUNDRED_NANOSECOND_INTERVAL;
  }


  @Override
  public String encodeStringValue(final Long value)
  {
    return String.valueOf(-value * ONE_HUNDRED_NANOSECOND_INTERVAL);
  }


  @Override
  public Class<Long> getType()
  {
    return Long.class;
  }
}
