/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AndFilter}.
 *
 * @author  Middleware Services
 */
public class AndFilterTest
{


  /**
   * And test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new AndFilter(new EqualityFilter("givenName", "John"), new EqualityFilter("sn", "Doe")),
          new byte[] {
            (byte) 0xa0, 0x1e,
            (byte) 0xa3, 0x11,
            0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6e, 0x4e, 0x61, 0x6d, 0x65,
            0x04, 0x04, 0x4a, 0x6f, 0x68, 0x6e,
            (byte) 0xa3, 0x09,
            0x04, 0x02, 0x73, 0x6e,
            0x04, 0x03, 0x44, 0x6f, 0x65, },
        },
        new Object[] {
          new AndFilter(),
          new byte[] {
            (byte) 0xa0, 0x00, },
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
  public void encode(final AndFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
