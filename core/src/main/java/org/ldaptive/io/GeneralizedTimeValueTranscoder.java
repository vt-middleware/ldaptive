/*
  $Id: GeneralizedTimeValueTranscoder.java 2994 2014-06-03 19:00:45Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2994 $
  Updated: $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
*/
package org.ldaptive.io;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decodes and encodes a generalized time for use in an ldap attribute value.
 * See http://tools.ietf.org/html/rfc4517#section-3.3.13
 *
 * @author  Middleware Services
 * @version  $Revision: 2994 $ $Date: 2014-06-03 15:00:45 -0400 (Tue, 03 Jun 2014) $
 */
public class GeneralizedTimeValueTranscoder
  extends AbstractStringValueTranscoder<Calendar>
{

  /** Pattern for capturing the year in generalized time. */
  private static final String YEAR_PATTERN = "(\\d{4})";

  /** Pattern for capturing the month in generalized time. */
  private static final String MONTH_PATTERN =
    "((?:\\x30[\\x31-\\x39])|(?:\\x31[\\x30-\\x32]))";

  /** Pattern for capturing the day in generalized time. */
  private static final String DAY_PATTERN =
    "((?:\\x30[\\x31-\\x39])" +
    "|(?:[\\x31-\\x32][\\x30-\\x39])" +
    "|(?:\\x33[\\x30-\\x31]))";

  /** Pattern for capturing hours in generalized time. */
  private static final String HOUR_PATTERN =
    "((?:[\\x30-\\x31][\\x30-\\x39])|(?:\\x32[\\x30-\\x33]))";

  /** Pattern for capturing optional minutes in generalized time. */
  private static final String MIN_PATTERN = "([\\x30-\\x35][\\x30-\\x39])?";

  /** Pattern for capturing optional seconds in generalized time. */
  private static final String SECOND_PATTERN = "([\\x30-\\x35][\\x30-\\x39])?";

  /** Pattern for capturing optional fraction in generalized time. */
  private static final String FRACTION_PATTERN = "([,.](\\d+))?";

  /** Pattern for capturing timezone in generalized time. */
  private static final String TIMEZONE_PATTERN =
    "(Z|(?:[+-]" + HOUR_PATTERN + MIN_PATTERN + "))";

  /** Generalized time format regular expression. */
  private static final Pattern TIME_REGEX = Pattern.compile(
    YEAR_PATTERN +
    MONTH_PATTERN +
    DAY_PATTERN +
    HOUR_PATTERN +
    MIN_PATTERN +
    SECOND_PATTERN +
    FRACTION_PATTERN +
    TIMEZONE_PATTERN);

  /** UTC time zone. */
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

  /** Default locale. */
  private static final Locale DEFAULT_LOCALE = Locale.getDefault();

  /** Thread local container holding date format which is not thread safe. */
  private static final ThreadLocal<DateFormat> DATE_FORMAT =
    new ThreadLocal<DateFormat>() {

      /** {@inheritDoc} */
      @Override
      protected DateFormat initialValue()
      {
        final SimpleDateFormat format = new SimpleDateFormat(
          "yyyyMMddHHmmss.SSS'Z'");
        format.setTimeZone(UTC);
        return format;
      }
    };

  /** Describes the fractional part of a generalized time string. */
  private static enum FractionalPart {

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


  /** {@inheritDoc} */
  @Override
  public Calendar decodeStringValue(final String value)
  {
    try {
      return parseGeneralizedTime(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public String encodeStringValue(final Calendar value)
  {
    final DateFormat format = DATE_FORMAT.get();
    return format.format(value.getTime());
  }


  /** {@inheritDoc} */
  @Override
  public Class<Calendar> getType()
  {
    return Calendar.class;
  }


  /**
   * Parses the supplied value and sets a calendar with the appropriate fields.
   *
   * @param  value  of generalized time to parse
   *
   * @return  calendar initialized to the correct time
   *
   * @throws  ParseException  if the value does not contain correct generalized
   * time syntax
   */
  protected Calendar parseGeneralizedTime(final String value)
    throws ParseException
  {
    if (value == null) {
      throw new IllegalArgumentException("String to parse cannot be null.");
    }

    final Matcher m = TIME_REGEX.matcher(value);
    if (!m.matches()) {
      throw new ParseException(
        "Invalid generalized time string.",
        value.length());
    }

    // CheckStyle:MagicNumber OFF
    // Get calendar in correct time zone
    final String tzString = m.group(9);
    final TimeZone tz;
    if ("Z".equals(tzString)) {
      tz = UTC;
    } else {
      tz = TimeZone.getTimeZone("GMT" + tzString);
    }

    final Calendar calendar = Calendar.getInstance(tz, DEFAULT_LOCALE);

    // Initialize calendar and impose strict calendrical field constraints
    calendar.setTimeInMillis(0);
    calendar.setLenient(false);

    // Set required time fields
    calendar.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
    calendar.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
    calendar.set(Calendar.DATE, Integer.parseInt(m.group(3)));
    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(4)));

    FractionalPart fraction = FractionalPart.Hours;

    // Set optional minutes
    int minutes = 0;
    if (m.group(5) != null) {
      fraction = FractionalPart.Minutes;
      minutes = Integer.parseInt(m.group(5));
    }
    calendar.set(Calendar.MINUTE, minutes);

    // Set optional seconds
    int seconds = 0;
    if (m.group(6) != null) {
      fraction = FractionalPart.Seconds;
      seconds = Integer.parseInt(m.group(6));
    }
    calendar.set(Calendar.SECOND, seconds);

    // Set optional fractional part
    calendar.add(Calendar.MILLISECOND, 0);
    if (m.group(7) != null) {
      calendar.add(Calendar.MILLISECOND, fraction.toMillis(m.group(8)));
    }
    // CheckStyle:MagicNumber ON

    // Force calendar to calculate
    calendar.getTimeInMillis();

    // Relax calendrical field constraints
    calendar.setLenient(true);

    return calendar;
  }
}
