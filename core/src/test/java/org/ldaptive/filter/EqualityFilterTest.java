/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link EqualityFilter}.
 *
 * @author  Middleware Services
 */
public class EqualityFilterTest
{


  /**
   * Equality test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new EqualityFilter("uid", "jdoe"),
          new byte[] {
            (byte) 0xa3, 0x0b, 0x04, 0x03, 0x75, 0x69, 0x64, 0x04, 0x04, 0x6a, 0x64, 0x6f, 0x65, },
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
  public void encode(final EqualityFilter filter, final byte[] berValue)
    throws Exception
  {
    assertThat(filter.getEncoder().encode()).isEqualTo(berValue);
  }
}
