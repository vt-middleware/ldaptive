/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.transcode;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link DeltaTimeValueTranscoder}.
 *
 * @author  Middleware Services
 */
public class DeltaTimeValueTranscoderTest
{

  /** Transcoder to test. */
  private final DeltaTimeValueTranscoder transcoder = new DeltaTimeValueTranscoder();


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
        2592000000L,
        "-25920000000000",
      },
    };
  }


  /**
   * @param  millis  to compare
   * @param  deltaTime  ldap attribute string value
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transcode", dataProvider = "times")
  public void testTranscode(final Long millis, final String deltaTime)
    throws Exception
  {
    assertThat(transcoder.decodeStringValue(deltaTime)).isEqualTo(millis);
    assertThat(transcoder.encodeStringValue(millis)).isEqualTo(deltaTime);
  }
}
