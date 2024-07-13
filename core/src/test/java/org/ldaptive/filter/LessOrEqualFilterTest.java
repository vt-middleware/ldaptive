/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link LessOrEqualFilter}.
 *
 * @author  Middleware Services
 */
public class LessOrEqualFilterTest
{


  /**
   * Less or equal test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new LessOrEqualFilter("accountBalance", "1234"),
          new byte[] {
            (byte) 0xa6, 0x16,
            0x04, 0x0e, 0x61, 0x63, 0x63, 0x6f, 0x75, 0x6e, 0x74, 0x42, 0x61, 0x6c, 0x61, 0x6e, 0x63, 0x65,
            0x04, 0x04, 0x31, 0x32, 0x33, 0x34, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "component")
  public void encode(final LessOrEqualFilter filter, final byte[] berValue)
    throws Exception
  {
    assertThat(filter.getEncoder().encode()).isEqualTo(berValue);
  }
}
