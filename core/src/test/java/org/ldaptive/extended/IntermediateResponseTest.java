/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.nio.charset.StandardCharsets;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link IntermediateResponse}.
 *
 * @author  Middleware Services
 */
public class IntermediateResponseTest
{


  /**
   * Intermediate response test data.
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
            // preamble
            0x30, 0x2c, 0x02, 0x01, 0x02,
            // intermediate response
            0x79, 0x27,
            // response name
            (byte) 0x80, 0x18, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x34, 0x32, 0x30,
            0x33, 0x2e, 0x31, 0x2e, 0x39, 0x2e, 0x31, 0x2e, 0x34,
            // response value
            (byte) 0x81, 0x0b, (byte) 0x80, 0x09, 0x4e, 0x6f, 0x6d, 0x4e, 0x6f, 0x6d, 0x4e, 0x6f, 0x6d},
          IntermediateResponse.builder()
            .messageID(2)
            .responseName("1.3.6.1.4.1.4203.1.9.1.4")
            .responseValue("NomNomNom".getBytes(StandardCharsets.UTF_8)).build(),
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
  public void encode(final byte[] berValue, final IntermediateResponse response)
    throws Exception
  {
    Assert.assertEquals(new IntermediateResponse(new DefaultDERBuffer(berValue)), response);
  }
}
