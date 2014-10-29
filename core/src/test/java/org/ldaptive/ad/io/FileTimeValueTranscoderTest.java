/*
  $Id: FileTimeValueTranscoderTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.ad.io;

import java.util.Calendar;
import java.util.TimeZone;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link FileTimeValueTranscoder}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class FileTimeValueTranscoderTest
{

  /** Transcoder to test. */
  private final FileTimeValueTranscoder transcoder =
    new FileTimeValueTranscoder();


  /**
   * Time test data.
   *
   * @return  time test data
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "times")
  public Object[][] createDates()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          createCalendar("UTC", 2014, 1, 28, 21, 54, 27, 711),
          "130354196677110000",
        },
      };
  }


  /**
   * @param  date  to compare
   * @param  fileTime  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"io"},
    dataProvider = "times"
  )
  public void testTranscode(final Calendar date, final String fileTime)
    throws Exception
  {
    Assert.assertEquals(transcoder.decodeStringValue(fileTime), date);
    Assert.assertEquals(transcoder.encodeStringValue(date), fileTime);
  }


  /**
   * Creates a calendar for testing.
   *
   * @param  timezone  of the calendar
   * @param  values  corresponding to calendar fields
   *
   * @return  calendar
   */
  protected Calendar createCalendar(final String timezone, final int... values)
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(0);
    calendar.setTimeZone(TimeZone.getTimeZone(timezone));
    for (int i = 0; i < values.length; i++) {
      switch (i) {

      case 0:
        calendar.set(Calendar.YEAR, values[i]);
        break;

      case 1:
        calendar.set(Calendar.MONTH, values[i] - 1);
        break;

      case 2:
        calendar.set(Calendar.DATE, values[i]);
        break;

      case 3:
        calendar.set(Calendar.HOUR_OF_DAY, values[i]);
        break;

      case 4:
        calendar.set(Calendar.MINUTE, values[i]);
        break;

      case 5:
        calendar.set(Calendar.SECOND, values[i]);
        break;

      case 6:
        calendar.set(Calendar.MILLISECOND, values[i]);
        break;

      default:
        throw new IllegalArgumentException("Too many values");
      }
    }
    calendar.getTimeInMillis();
    return calendar;
  }
}
