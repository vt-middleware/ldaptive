/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.ByteBuffer;
import org.ldaptive.ResultCode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AddResponse}.
 *
 * @author  Middleware Services
 */
public class AddResponseTest
{


  /**
   * Add response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x02,
            // add response
            0x69, 0x07,
            // success
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          new AddResponse.Builder().messageID(2)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
      };
  }

  /**
   * @param  berValue  encoded response.
   * @param  response  expected decoded response.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "response")
  public void encode(final byte[] berValue, final AddResponse response)
    throws Exception
  {
    Assert.assertEquals(new AddResponse(ByteBuffer.wrap(berValue)), response);
  }
}
