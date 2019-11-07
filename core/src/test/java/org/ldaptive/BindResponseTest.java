/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link BindResponse}.
 *
 * @author  Middleware Services
 */
public class BindResponseTest
{


  /**
   * Bind response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          // success bind response
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x01,
            // bind response
            0x61, 0x07,
            // success result
            0x0a, 0x01, 0x00,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          BindResponse.builder().messageID(1)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
        new Object[] {
          // SASL bind response
          new byte[] {
            //preamble
            0x30, 0x30, 0x02, 0x01, 0x01,
            // bind response
            0x61, 0x2b,
            // success result
            0x0a, 0x01, 0x0e,
            // no matched DN
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // SASL credentials
            (byte) 0x87, 0x22, 0x3c, 0x31, 0x30, 0x61, 0x31, 0x33, 0x63, 0x37, 0x62, 0x66, 0x37, 0x30, 0x38, 0x63, 0x61,
            0x30, 0x66, 0x33, 0x39, 0x39, 0x63, 0x61, 0x39, 0x39, 0x65, 0x39, 0x32, 0x37, 0x64, 0x61, 0x38, 0x38, 0x62,
            0x3e},
          BindResponse.builder()
            .messageID(1)
            .resultCode(ResultCode.SASL_BIND_IN_PROGRESS)
            .matchedDN("")
            .diagnosticMessage("")
            .serverSaslCreds("<10a13c7bf708ca0f399ca99e927da88b>").build(),
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
  public void encode(final byte[] berValue, final BindResponse response)
    throws Exception
  {
    Assert.assertEquals(new BindResponse(new DefaultDERBuffer(berValue)), response);
  }
}
