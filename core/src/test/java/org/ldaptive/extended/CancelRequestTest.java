/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CancelRequest}.
 *
 * @author  Middleware Services
 */
public class CancelRequestTest
{


  /**
   * Cancel test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new CancelRequest(1),
          new byte[] {
            // preamble
            0x30, 0x19, 0x02, 0x01, 0x01, 0x77, 0x14,
            // oid
            (byte) 0x80, 0x0b, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x31, 0x2e, 0x38,
            // extended request value
            (byte) 0x81, 0x05, 0x30, 0x03, 0x02, 0x01, 0x01},
        },
      };
  }

  /**
   * @param  request  cancel request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final CancelRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(1), berValue);
  }
}
