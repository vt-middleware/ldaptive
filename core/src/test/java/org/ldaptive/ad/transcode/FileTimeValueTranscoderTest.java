/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.transcode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link FileTimeValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class FileTimeValueTranscoderTest
{

  /** Transcoder to test. */
  private final FileTimeValueTranscoder transcoder = new FileTimeValueTranscoder();


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
    return new Object[][] {
      new Object[] {
        createDateTime("Z", 2014, 1, 28, 21, 54, 27, 711),
        "130354196677110000",
      },
      new Object[] {
        createDateTime("Z", 2016, 2, 18, 15, 31, 32, 327),
        "131002830923270000",
      },
    };
  }


  /**
   * @param  date  to compare
   * @param  fileTime  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "times")
  public void decodeStringValue(final ZonedDateTime date, final String fileTime)
    throws Exception
  {
    assertThat(transcoder.decodeStringValue(fileTime)).isEqualTo(date);
  }


  /**
   * @param  date  to compare
   * @param  fileTime  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "times")
  public void encodeStringValue(final ZonedDateTime date, final String fileTime)
    throws Exception
  {
    assertThat(transcoder.encodeStringValue(date)).isEqualTo(fileTime);
  }


  /**
   * Creates a date time for testing.
   *
   * @param  timezone  of the date time
   * @param  values  corresponding to date time fields
   *
   * @return  date time
   */
  protected ZonedDateTime createDateTime(final String timezone, final int... values)
  {
    return ZonedDateTime.of(
      LocalDateTime.of(
        values[0],
        values[1],
        values[2],
        values[3],
        values.length > 4 ? values[4] : 0,
        values.length > 5 ? values[5] : 0).plus(values.length > 6 ? values[6] : 0, ChronoUnit.MILLIS),
      ZoneId.of(timezone));
  }
}
