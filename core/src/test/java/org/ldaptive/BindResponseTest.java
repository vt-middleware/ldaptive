/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.nio.charset.StandardCharsets;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
            // preamble
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
            // preamble
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
            .serverSaslCreds("<10a13c7bf708ca0f399ca99e927da88b>".getBytes(StandardCharsets.UTF_8)).build(),
        },
        new Object[] {
          // AD bind response
          new byte[] {
            // preamble
            0x30, (byte) 0x84, 0x00, 0x00, 0x00, 0x66, 0x02, 0x01, 0x02,
            // bind response
            0x61, (byte) 0x84, 0x00, 0x00, 0x00, 0x5d,
            // invalid credentials result
            0x0a, 0x01, 0x31,
            // no matched DN
            0x04, 0x00,
            // diagnostic message
            0x04, 0x56,
            0x38, 0x30, 0x30, 0x39, 0x30, 0x33, 0x30, 0x38, 0x3a, 0x20, 0x4c, 0x64, 0x61, 0x70, 0x45, 0x72, 0x72, 0x3a,
            0x20, 0x44, 0x53, 0x49, 0x44, 0x2d, 0x30, 0x43, 0x30, 0x39, 0x30, 0x33, 0x41, 0x39, 0x2c, 0x20, 0x63, 0x6f,
            0x6d, 0x6d, 0x65, 0x6e, 0x74, 0x3a, 0x20, 0x41, 0x63, 0x63, 0x65, 0x70, 0x74, 0x53, 0x65, 0x63, 0x75, 0x72,
            0x69, 0x74, 0x79, 0x43, 0x6f, 0x6e, 0x74, 0x65, 0x78, 0x74, 0x20, 0x65, 0x72, 0x72, 0x6f, 0x72, 0x2c, 0x20,
            0x64, 0x61, 0x74, 0x61, 0x20, 0x30, 0x2c, 0x20, 0x76, 0x31, 0x64, 0x62, 0x31, 0x00},
          BindResponse.builder()
            .messageID(2)
            .resultCode(ResultCode.INVALID_CREDENTIALS)
            .matchedDN("")
            .diagnosticMessage(
              "80090308: LdapErr: DSID-0C0903A9, comment: AcceptSecurityContext error, data 0, v1db1\0")
            .build(),
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
    assertThat(new BindResponse(new DefaultDERBuffer(berValue))).isEqualTo(response);
  }
}
