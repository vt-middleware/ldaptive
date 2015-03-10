/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.util.Calendar;
import java.util.TimeZone;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link GeneralizedTimeValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class GeneralizedTimeValueTranscoderTest
{

  /** Transcoder to test. */
  private final GeneralizedTimeValueTranscoder transcoder = new GeneralizedTimeValueTranscoder();


  /**
   * Date test data.
   *
   * @return  dates
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "dates")
  public Object[][] createDates()
    throws Exception
  {
    return
      new Object[][] {
        // no fraction
        new Object[] {
          createCalendar("UTC", 1990, 10, 23, 17),
          "1990102317Z",
          "19901023170000.000Z",
        },
        new Object[] {
          createCalendar("GMT+04", 2007, 10, 27, 13),
          "2007102713+04",
          "20071027090000.000Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 1993, 12, 15, 14),
          "1993121514-1030",
          "19931216003000.000Z",
        },
        // dot fraction
        new Object[] {
          createCalendar("UTC", 2006, 8, 18, 23, 35, 9, 600),
          "2006081823.586Z",
          "20060818233509.600Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2012, 2, 15, 2, 15, 0, 0),
          "2012021502.250+0100",
          "20120215011500.000Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 2005, 6, 4, 5, 52, 55, 200),
          "2005060405.882-1030",
          "20050604162255.200Z",
        },
        // comma fraction
        new Object[] {
          createCalendar("UTC", 1992, 3, 1, 7, 20, 38, 400),
          "1992030107,344Z",
          "19920301072038.400Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2008, 3, 21, 23, 30, 14, 400),
          "2008032123,504+0100",
          "20080321223014.400Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 2018, 4, 8, 6, 45, 43, 200),
          "2018040806,762-1030",
          "20180408171543.200Z",
        },
        // min, no fraction
        new Object[] {
          createCalendar("UTC", 1994, 6, 7, 11, 50),
          "199406071150Z",
          "19940607115000.000Z",
        },
        new Object[] {
          createCalendar("GMT+04", 2015, 8, 24, 12, 41),
          "201508241241+04",
          "20150824084100.000Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 2012, 12, 19, 7, 31),
          "201212190731-1030",
          "20121219180100.000Z",
        },
        // min, dot fraction
        new Object[] {
          createCalendar("UTC", 2019, 5, 27, 13, 57, 46, 800),
          "201905271357.780Z",
          "20190527135746.800Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2001, 2, 23, 17, 32, 33, 180),
          "200102231732.553+0100",
          "20010223163233.180Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 1997, 10, 27, 19, 32, 31, 860),
          "199710271932.531-1030",
          "19971028060231.860Z",
        },
        // min, comma fraction
        new Object[] {
          createCalendar("UTC", 2005, 1, 6, 7, 5, 19, 260),
          "200501060705,321Z",
          "20050106070519.260Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2019, 10, 1, 3, 53, 16, 260),
          "201910010353,271+0100",
          "20191001025316.260Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 2003, 2, 4, 10, 56, 49, 560),
          "200302041056,826-1030",
          "20030204212649.560Z",
        },
        // min, sec, no fraction
        new Object[] {
          createCalendar("UTC", 1993, 3, 4, 1, 6, 45),
          "19930304010645Z",
          "19930304010645.000Z",
        },
        new Object[] {
          createCalendar("GMT+04", 1996, 11, 15, 2, 1, 33),
          "19961115020133+04",
          "19961114220133.000Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 2012, 11, 4, 10, 37, 40),
          "20121104103740-1030",
          "20121104210740.000Z",
        },
        // min, sec, dot fraction
        new Object[] {
          createCalendar("UTC", 2016, 5, 17, 5, 31, 50, 193),
          "20160517053150.193Z",
          "20160517053150.193Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2006, 9, 28, 9, 50, 56, 142),
          "20060928095056.142+0100",
          "20060928085056.142Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 1997, 7, 15, 17, 41, 7, 418),
          "19970715174107.418-1030",
          "19970716041107.418Z",
        },
        // min, sec, comma fraction
        new Object[] {
          createCalendar("UTC", 2003, 5, 11, 3, 55, 24, 11),
          "20030511035524,011Z",
          "20030511035524.011Z",
        },
        new Object[] {
          createCalendar("GMT+0100", 2000, 9, 8, 4, 38, 1, 536),
          "20000908043801,536+0100",
          "20000908033801.536Z",
        },
        new Object[] {
          createCalendar("GMT-1030", 1999, 5, 12, 3, 34, 16, 307),
          "19990512033416.307-1030",
          "19990512140416.307Z",
        },
        // feb 29 in leap year
        new Object[] {
          createCalendar("UTC", 2012, 2, 29, 0),
          "2012022900Z",
          "20120229000000.000Z",
        },
      };
  }


  /**
   * Date test data.
   *
   * @return  dates
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "invalid")
  public Object[][] createInvalidSyntax()
    throws Exception
  {
    return
      new Object[][] {
        // invalid month
        new Object[] {"20050003193252Z"},
        new Object[] {"20011315121902Z"},
        new Object[] {"20065205051238Z"},
        new Object[] {"19989912035214Z"},
        // invalid day
        new Object[] {"20130600153448Z"},
        new Object[] {"19970132100613Z"},
        new Object[] {"19990466184045Z"},
        new Object[] {"20180299184327Z"},
        // invalid hour
        new Object[] {"19991104604515Z"},
        new Object[] {"19990611775328Z"},
        new Object[] {"20161126993226Z"},
        // invalid min
        new Object[] {"20150120036044Z"},
        new Object[] {"19950801078428Z"},
        new Object[] {"20141110059918Z"},
        // invalid sec
        new Object[] {"19970507065463Z"},
        new Object[] {"20010804024188Z"},
        new Object[] {"20100401071599Z"},
        // invalid timezone
        new Object[] {"20110118183832Z+"},
        new Object[] {"20140828202734+2400"},
        new Object[] {"20020505162928+9900"},
        new Object[] {"20160404014028+1260"},
        new Object[] {"20020522033010+1299"},
        // leap second
        new Object[] {"19981126110460Z"},
        // too short
        new Object[] {"200"},
        new Object[] {"2009"},
        new Object[] {"20091"},
        new Object[] {"200912"},
        new Object[] {"2009122"},
        new Object[] {"20091220"},
        new Object[] {"200912200"},
        new Object[] {"2009122003"},
        // invalid characters
        new Object[] {"2AA9122003Z"},
        new Object[] {"2009122003Y"},
        new Object[] {"2004072704BBZ"},
        new Object[] {"200407270431CCZ"},
        new Object[] {"20040727043108.DZ"},
        new Object[] {"20040727043108.DDZ"},
        new Object[] {"20040727043108.DDDZ"},
        // missing timezone
        new Object[] {"2017092602"},
        new Object[] {"201709260206"},
        new Object[] {"20170926020649"},
        new Object[] {"20170926020649.5"},
        new Object[] {"20170926020649.56"},
        new Object[] {"20170926020649.567"},
        new Object[] {"20170926020649,2"},
        new Object[] {"20170926020649,23"},
        new Object[] {"20170926020649,234"},
        // invalid timezone character
        new Object[] {"2010112415E"},
        new Object[] {"201011241520E"},
        new Object[] {"20101124152005E"},
        new Object[] {"20101124152005.1E"},
        new Object[] {"20101124152005.12E"},
        new Object[] {"20101124152005.122E"},
        new Object[] {"20130915044541ZF"},
        new Object[] {"20101124152005+0630F"},
        // missing fraction
        new Object[] {"20020511040917.Z"},
        new Object[] {"20020511040917,Z"},
        // feb 29 in non-leap year
        new Object[] {"2011022900Z"},
        new Object[] {""},
        new Object[] {null},
      };
  }


  /**
   * @param  date  to compare
   * @param  generalizedTime  ldap attribute string value
   * @param  formatTime  formatted ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"io"}, dataProvider = "dates")
  public void testTranscode(final Calendar date, final String generalizedTime, final String formatTime)
    throws Exception
  {
    Assert.assertEquals(transcoder.decodeStringValue(generalizedTime), date);
    Assert.assertEquals(transcoder.encodeStringValue(date), formatTime);

    final Calendar c = (Calendar) date.clone();
    c.setTimeZone(TimeZone.getTimeZone("UTC"));
    Assert.assertEquals(transcoder.decodeStringValue(formatTime), c);
  }


  /**
   * @param  generalizedTime  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"io"}, dataProvider = "invalid")
  public void testInvalid(final String generalizedTime)
    throws Exception
  {
    try {
      transcoder.decodeStringValue(generalizedTime);
      Assert.fail("Should have thrown exception");
    } catch (Exception e) {
      Assert.assertEquals(e.getClass(), IllegalArgumentException.class);
    }
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
