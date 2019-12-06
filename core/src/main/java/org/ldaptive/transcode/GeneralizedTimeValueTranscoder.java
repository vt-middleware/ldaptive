/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transcode;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decodes and encodes a generalized time for use in an ldap attribute value. See
 * http://tools.ietf.org/html/rfc4517#section-3.3.13
 *
 * @author  Middleware Services
 */
public class GeneralizedTimeValueTranscoder extends AbstractStringValueTranscoder<ZonedDateTime>
{

  /** Pattern for capturing the year in generalized time. */
  private static final String YEAR_PATTERN = "(\\d{4})";

  /** Pattern for capturing the month in generalized time. */
  private static final String MONTH_PATTERN = "((?:\\x30[\\x31-\\x39])|(?:\\x31[\\x30-\\x32]))";

  /** Pattern for capturing the day in generalized time. */
  private static final String DAY_PATTERN = "((?:\\x30[\\x31-\\x39])" +
    "|(?:[\\x31-\\x32][\\x30-\\x39])" +
    "|(?:\\x33[\\x30-\\x31]))";

  /** Pattern for capturing hours in generalized time. */
  private static final String HOUR_PATTERN = "((?:[\\x30-\\x31][\\x30-\\x39])|(?:\\x32[\\x30-\\x33]))";

  /** Pattern for capturing optional minutes in generalized time. */
  private static final String MIN_PATTERN = "([\\x30-\\x35][\\x30-\\x39])?";

  /** Pattern for capturing optional seconds in generalized time. */
  private static final String SECOND_PATTERN = "([\\x30-\\x35][\\x30-\\x39])?";

  /** Pattern for capturing optional fraction in generalized time. */
  private static final String FRACTION_PATTERN = "([,.](\\d+))?";

  /** Pattern for capturing timezone in generalized time. */
  private static final String TIMEZONE_PATTERN = "(Z|(?:[+-]" + HOUR_PATTERN + MIN_PATTERN + "))";

  /** Generalized time format regular expression. */
  private static final Pattern TIME_REGEX = Pattern.compile(
    YEAR_PATTERN + MONTH_PATTERN + DAY_PATTERN + HOUR_PATTERN + MIN_PATTERN + SECOND_PATTERN + FRACTION_PATTERN +
    TIMEZONE_PATTERN);

  /** Date format. */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS'Z'");

  /** Describes the fractional part of a generalized time string. */
  private enum FractionalPart {

    /** Fractional hours. */
    Hours(3600000),

    /** Fractional minutes. */
    Minutes(60000),

    /** Fractional seconds. */
    Seconds(1000);

    /** Scale factor to convert units to millis. */
    private final int scaleFactor;


    /**
     * Creates a new fractional part.
     *
     * @param  scale  scale factor.
     */
    FractionalPart(final int scale)
    {
      scaleFactor = scale;
    }


    /**
     * Converts the given fractional date part to milliseconds.
     *
     * @param  fraction  digits of fractional date part
     *
     * @return  fraction converted to milliseconds.
     */
    int toMillis(final String fraction)
    {
      return (int) (Double.parseDouble('.' + fraction) * scaleFactor);
    }
  }


  @Override
  public ZonedDateTime decodeStringValue(final String value)
  {
    try {
      return parseGeneralizedTime(value);
    } catch (ParseException | DateTimeException e) {
      throw new IllegalArgumentException(e);
    }
  }


  @Override
  public String encodeStringValue(final ZonedDateTime value)
  {
    if (value.getZone().normalized().equals(ZoneOffset.UTC)) {
      return value.format(DATE_FORMAT);
    } else {
      return value.withZoneSameInstant(ZoneOffset.UTC).format(DATE_FORMAT);
    }
  }


  @Override
  public Class<ZonedDateTime> getType()
  {
    return ZonedDateTime.class;
  }


  /**
   * Parses the supplied value and returns a date time.
   *
   * @param  value  of generalized time to parse
   *
   * @return  date time initialized to the correct time
   *
   * @throws  ParseException  if the value does not contain correct generalized time syntax
   */
  protected ZonedDateTime parseGeneralizedTime(final String value)
    throws ParseException
  {
    if (value == null) {
      throw new IllegalArgumentException("String to parse cannot be null.");
    }

    final Matcher m = TIME_REGEX.matcher(value);
    if (!m.matches()) {
      throw new ParseException("Invalid generalized time string.", value.length());
    }

    // CheckStyle:MagicNumber OFF
    final ZoneId zoneId;
    final String tzString = m.group(9);
    if ("Z".equals(tzString)) {
      zoneId = ZoneOffset.UTC;
    } else {
      zoneId = ZoneId.of("GMT" + tzString);
    }

    // Set required time fields
    final int year = Integer.parseInt(m.group(1));
    final int month = Integer.parseInt(m.group(2));
    final int dayOfMonth = Integer.parseInt(m.group(3));
    final int hour = Integer.parseInt(m.group(4));

    FractionalPart fraction = FractionalPart.Hours;

    // Set optional minutes
    int minutes = 0;
    if (m.group(5) != null) {
      fraction = FractionalPart.Minutes;
      minutes = Integer.parseInt(m.group(5));
    }

    // Set optional seconds
    int seconds = 0;
    if (m.group(6) != null) {
      fraction = FractionalPart.Seconds;
      seconds = Integer.parseInt(m.group(6));
    }

    // Set optional fractional part
    int millis = 0;
    if (m.group(7) != null) {
      millis = fraction.toMillis(m.group(8));
    }
    // CheckStyle:MagicNumber ON

    return ZonedDateTime.of(
      LocalDateTime.of(year, month, dayOfMonth, hour, minutes, seconds).plus(millis, ChronoUnit.MILLIS), zoneId);
  }
}
