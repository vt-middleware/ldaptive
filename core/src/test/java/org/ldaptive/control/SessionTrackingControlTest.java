/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SessionTrackingControl}.
 *
 * @author  Middleware Services
 */
public class SessionTrackingControlTest
{


  /**
   * Session tracking control test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request-response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x42, 0x04, 0x09, 0x31, 0x39, 0x32, 0x2e, 0x30, 0x2e, 0x32, 0x2e, 0x31, 0x04, 0x0f, 0x61, 0x70,
              0x70, 0x2e, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2e, 0x63, 0x6f, 0x6d, 0x04, 0x1c, 0x31, 0x2e,
              0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x32, 0x31, 0x30, 0x30, 0x38, 0x2e, 0x31,
              0x30, 0x38, 0x2e, 0x36, 0x33, 0x2e, 0x31, 0x2e, 0x33, 0x04, 0x06, 0x62, 0x6c, 0x6f, 0x67, 0x67, 0x73}),
          new SessionTrackingControl("192.0.2.1", "app.example.com", "1.3.6.1.4.1.21008.108.63.1.3", "bloggs"),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  control  session tracking control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request-response")
  public void encode(final DERBuffer berValue, final SessionTrackingControl control)
    throws Exception
  {
    assertThat(control.encode()).isEqualTo(berValue.getRemainingBytes());
  }


  /**
   * @param  berValue  to decode.
   * @param  control  session tracking control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request-response")
  public void decode(final DERBuffer berValue, final SessionTrackingControl control)
    throws Exception
  {
    final SessionTrackingControl actual = new SessionTrackingControl();
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
