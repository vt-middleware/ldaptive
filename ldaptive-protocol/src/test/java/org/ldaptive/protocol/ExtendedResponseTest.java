/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.nio.charset.StandardCharsets;
import org.ldaptive.ResultCode;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ExtendedResponse}.
 *
 * @author  Middleware Services
 */
public class ExtendedResponseTest
{


  /**
   * Extended response test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // startTLS success response, no oid, no value
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x0c, 0x02, 0x01, 0x01,
            // extended response
            0x78, 0x07,
            // success
            0x0a, 0x01, 0x00,
            // no matched dn
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00},
          new ExtendedResponse.Builder().messageID(1)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("").build(),
        },
        // whoami success response, no oid
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x3A, 0x02, 0x01, 0x03,
            // extended response
            0x78, 0x35,
            // success
            0x0a, 0x01, 0x00,
            // no matched dn
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // response value
            (byte) 0x8b, 0x2c, 0x64, 0x6e, 0x3a, 0x63, 0x6e, 0x3d, 0x4a, 0x6f, 0x68, 0x6e, 0x20, 0x51, 0x75, 0x69, 0x6e,
            0x63, 0x79, 0x20, 0x41, 0x64, 0x61, 0x6d, 0x73, 0x2c, 0x6f, 0x75, 0x3d, 0x74, 0x65, 0x73, 0x74, 0x2c, 0x64,
            0x63, 0x3d, 0x76, 0x74, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x64, 0x75},
          new ExtendedResponse.Builder().messageID(3)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .responseValue("dn:cn=John Quincy Adams,ou=test,dc=vt,dc=edu".getBytes(StandardCharsets.UTF_8)).build(),
        },
        // response with diagnostic message
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x2c, 0x02, 0x01, 0x02,
            // extended response
            0x78, 0x27,
            // unwilling to perform
            0x0a, 0x01, 0x35,
            // no matched dn
            0x04, 0x00,
            // diagnostic message
            0x04, 0x20, 0x75, 0x6e, 0x77, 0x69, 0x6c, 0x6c, 0x69, 0x6e, 0x67, 0x20, 0x74, 0x6f, 0x20, 0x76, 0x65, 0x72,
            0x69, 0x66, 0x79, 0x20, 0x6f, 0x6c, 0x64, 0x20, 0x70, 0x61, 0x73, 0x73, 0x77, 0x6f, 0x72, 0x64},
          new ExtendedResponse.Builder().messageID(2)
            .resultCode(ResultCode.UNWILLING_TO_PERFORM)
            .matchedDN("")
            .diagnosticMessage("unwilling to verify old password").build(),
        },
        // response with value, no oid
        new Object[] {
          new byte[] {
            //preamble
            0x30, 0x1a, 0x02, 0x01, 0x04,
            // extended response
            0x78, 0x15,
            // success
            0x0a, 0x01, 0x00,
            // no matched dn
            0x04, 0x00,
            // no diagnostic message
            0x04, 0x00,
            // response value
            (byte) 0x8b, 0x0c, 0x30, 0x0a, (byte) 0x80, 0x08, 0x43, 0x6d, 0x33, 0x47, 0x6b, 0x79, 0x44, 0x61},
          new ExtendedResponse.Builder().messageID(4)
            .resultCode(ResultCode.SUCCESS)
            .matchedDN("")
            .diagnosticMessage("")
            .responseValue(
              new byte[] {0x30, 0x0a, (byte) 0x80, 0x08, 0x43, 0x6d, 0x33, 0x47, 0x6b, 0x79, 0x44, 0x61}).build(),
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
  public void encode(final byte[] berValue, final ExtendedResponse response)
    throws Exception
  {
    Assert.assertEquals(new ExtendedResponse(new DefaultDERBuffer(berValue)), response);
  }
}
