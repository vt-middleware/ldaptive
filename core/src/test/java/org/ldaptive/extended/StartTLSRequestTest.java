/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link StartTLSRequest}.
 *
 * @author  Middleware Services
 */
public class StartTLSRequestTest
{


  /**
   * StartTLS test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new StartTLSRequest(),
          new byte[] {
            // preamble
            0x30, 0x1d, 0x02, 0x01, 0x01, 0x77, 0x18,
            // oid
            (byte) 0x80, 0x16, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x31, 0x34, 0x36,
            0x36, 0x2e, 0x32, 0x30, 0x30, 0x33, 0x37},
        },
      };
  }


  /**
   * @param  request  starttls request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final StartTLSRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(1), berValue);
  }
}
