/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link PresenceFilter}.
 *
 * @author  Middleware Services
 */
public class PresenceFilterTest
{


  /**
   * Presence test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "encoded")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new PresenceFilter("uid"),
          new byte[] {
            (byte) 0x87, 0x03, 0x75, 0x69, 0x64, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "encoded")
  public void encode(final PresenceFilter filter, final byte[] berValue)
    throws Exception
  {
    assertThat(filter.getEncoder().encode()).isEqualTo(berValue);
  }
}
