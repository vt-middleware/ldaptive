/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link NotFilter}.
 *
 * @author  Middleware Services
 */
public class NotFilterTest
{


  /**
   * Not test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[]{
          new NotFilter(new EqualityFilter("givenName", "John")),
          new byte[]{
            (byte) 0xa2, 0x13,
            (byte) 0xa3, 0x11,
            0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6e, 0x4e, 0x61, 0x6d, 0x65,
            0x04, 0x04, 0x4a, 0x6f, 0x68, 0x6e, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "component")
  public void encode(final NotFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
