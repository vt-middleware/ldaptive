/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.ldaptive.ResultCode;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link NoticeOfDisconnection}.
 *
 * @author  Middleware Services
 */
public class NoticeOfDisconnectionTest
{


  /**
   * Notice of disconnection test data.
   *
   * @return  notice of disconnection test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          // notice of disconnection
          new byte[] {
            //preamble
            0x30, 0x49, 0x02, 0x01, 0x00,
            // protocol op
            0x78, 0x44,
            // unavailable result code
            0x0a, 0x01, 0x34,
            // no matched DN
            0x04, 0x00,
            // diagnostic message
            0x04, 0x25, 0x54, 0x68, 0x65, 0x20, 0x44, 0x69, 0x72, 0x65, 0x63, 0x74, 0x6f, 0x72, 0x79, 0x20, 0x53, 0x65,
            0x72, 0x76, 0x65, 0x72, 0x20, 0x69, 0x73, 0x20, 0x73, 0x68, 0x75, 0x74, 0x74, 0x69, 0x6e, 0x67, 0x20, 0x64,
            0x6f, 0x77, 0x6e,
            // response OID
            (byte) 0x8a, 0x16, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x31, 0x34, 0x36,
            0x36, 0x2e, 0x32, 0x30, 0x30, 0x33, 0x36},
          NoticeOfDisconnection.builder()
            .responseName(NoticeOfDisconnection.OID)
            .resultCode(ResultCode.UNAVAILABLE)
            .matchedDN("")
            .diagnosticMessage("The Directory Server is shutting down").build(),
        },
      };
  }


  /**
   * @param  berValue  encoded response.
   * @param  notification  expected decoded notification.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "response")
  public void encode(final byte[] berValue, final NoticeOfDisconnection notification)
    throws Exception
  {
    assertThat(new NoticeOfDisconnection(new DefaultDERBuffer(berValue))).isEqualTo(notification);
  }
}
