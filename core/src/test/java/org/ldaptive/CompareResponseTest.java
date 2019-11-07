/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CompareResponse}.
 *
 * @author  Middleware Services
 */
public class CompareResponseTest
{


  /**
   * Compare response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // true compare response
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x02,
            // compare response
            0x6f, 0x07,
            // true result
            0x0a, 0x01, 0x06,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          CompareResponse.builder().messageID(2)
            .resultCode(ResultCode.COMPARE_TRUE)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
        // false compare response
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x02,
            // compare response
            0x6f, 0x07,
            // false result
            0x0a, 0x01, 0x05,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          CompareResponse.builder().messageID(2)
            .resultCode(ResultCode.COMPARE_FALSE)
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
  @Test(dataProvider = "response")
  public void encode(final byte[] berValue, final CompareResponse response)
    throws Exception
  {
    Assert.assertEquals(new CompareResponse(new DefaultDERBuffer(berValue)), response);
  }
}
