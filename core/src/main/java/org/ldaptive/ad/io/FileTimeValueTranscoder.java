/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.io;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import org.ldaptive.io.AbstractStringValueTranscoder;

/**
 * Decodes and encodes an active directory file time value for use in an ldap
 * attribute value.
 *
 * @author  Middleware Services
 * @version  $Revision: 3006 $ $Date: 2014-07-02 10:22:50 -0400 (Wed, 02 Jul 2014) $
 */
public class FileTimeValueTranscoder
  extends AbstractStringValueTranscoder<Calendar>
{

  /** UTC time zone. */
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  /** Default locale. */
  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  /**
   * Number of milliseconds between standard Unix era (1/1/1970) and filetime
   * start (1/1/1601).
   */
  private static final long ERA_OFFSET = 11644473600000L;

  /**
   * File time uses 100-nanosecond intervals. For conversion purposes this is
   * 1x10^6 / 100.
   */
  private static final long ONE_HUNDRED_NANOSECOND_INTERVAL = 10000L;


  /** {@inheritDoc} */
  @Override
  public Calendar decodeStringValue(final String value)
  {
    final Calendar calendar = Calendar.getInstance(UTC, DEFAULT_LOCALE);
    calendar.setTimeInMillis(
      Long.parseLong(value) / ONE_HUNDRED_NANOSECOND_INTERVAL - ERA_OFFSET);
    return calendar;
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final Calendar value)
  {
    return
      String.valueOf(
        (value.getTimeInMillis() + ERA_OFFSET) *
        ONE_HUNDRED_NANOSECOND_INTERVAL);
  }


  /** {@inheritDoc} */
  @Override
  public Class<Calendar> getType()
  {
    return Calendar.class;
  }
}
