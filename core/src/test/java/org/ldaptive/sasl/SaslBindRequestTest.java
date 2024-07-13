/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SaslBindRequest}.
 *
 * @author  Middleware Services
 */
public class SaslBindRequestTest
{


  /**
   * SASL bind test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // CRAM-MD5 request 1
        new Object[] {
          SaslBindRequest.builder().mechanism("CRAM-MD5").build(),
          new byte[] {
            // preamble
            0x30, 0x16, 0x02, 0x01, 0x01, 0x60, 0x11, 0x02, 0x01, 0x03,
            // empty bind dn
            0x04, 0x00,
            // SASL auth mechanism
            (byte) 0xa3, 0x0a,
            // SASL auth mechanism name
            0x04, 0x08, 0x43, 0x52, 0x41, 0x4d, 0x2d, 0x4d, 0x44, 0x35, },
        },
        // CRAM-MD5 request 2
        new Object[] {
          SaslBindRequest.builder()
            .mechanism("CRAM-MD5")
            .credentials("u:jdoe d52116c87c31d9cc747600f9486d2a1d").build(),
          new byte[] {
            // preamble
            0x30, 0x3f, 0x02, 0x01, 0x01, 0x60, 0x3a, 0x02, 0x01, 0x03,
            // empty bind dn
            0x04, 0x00,
            // SASL auth mechanism
            (byte) 0xa3, 0x33,
            // SASL mechanism name
            0x04, 0x08, 0x43, 0x52, 0x41, 0x4d, 0x2d, 0x4d, 0x44, 0x35,
            // SASL mechanism credentials
            0x04, 0x27, 0x75, 0x3a, 0x6a, 0x64, 0x6f, 0x65, 0x20, 0x64, 0x35, 0x32, 0x31, 0x31, 0x36, 0x63, 0x38, 0x37,
            0x63, 0x33, 0x31, 0x64, 0x39, 0x63, 0x63, 0x37, 0x34, 0x37, 0x36, 0x30, 0x30, 0x66, 0x39, 0x34, 0x38, 0x36,
            0x64, 0x32, 0x61, 0x31, 0x64, },
        },
      };
  }


  /**
   * @param  request  SASL bind request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final SaslBindRequest request, final byte[] berValue)
    throws Exception
  {
    assertThat(request.encode(1)).isEqualTo(berValue);
  }
}
