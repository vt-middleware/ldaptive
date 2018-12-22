/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AbandonRequest}.
 *
 * @author  Middleware Services
 */
public class AbandonRequestTest
{


  /**
   * Abandon test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new AbandonRequest(1),
          new byte[] {
            // preamble
            0x30, 0x06, 0x02, 0x01, 0x02,
            // abandon request number 1
            0x50, 0x01, 0x01},
        },
      };
  }

  /**
   * @param  request  abandon request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "request")
  public void encode(final AbandonRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(2), berValue);
  }
}
