/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link WhoAmIRequest}.
 *
 * @author  Middleware Services
 */
public class WhoAmIRequestTest
{


  /**
   * Who am i test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new WhoAmIRequest(),
          new byte[] {
            // preamble
            0x30, 0x1e, 0x02, 0x01, 0x02, 0x77, 0x19,
            // oid
            (byte) 0x80, 0x17, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x34, 0x32, 0x30,
            0x33, 0x2e, 0x31, 0x2e, 0x31, 0x31, 0x2e, 0x33},
        },
      };
  }


  /**
   * @param  request  who am i request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final WhoAmIRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(2), berValue);
  }
}
