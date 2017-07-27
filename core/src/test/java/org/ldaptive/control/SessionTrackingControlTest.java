/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
        // BER: 30:42:04:09:31:39:32:2e:30:2e:32:2e:31:04:0f:61:70:70:2e:65:78:61:6d:70:6c:65:2e:63:6f:6d
        //      04:1c:31:2e:33:2e:36:2e:31:2e:34:2e:31:2e:32:31:30:30:38:2e:31:30:38:2e:36:33:2e:31:2e:33
        //      04:06:62:6c:6f:67:67:73
        new Object[] {
          LdapUtils.base64Decode(
            "MEIECTE5Mi4wLjIuMQQPYXBwLmV4YW1wbGUuY29tBBwxLjMuNi4xLjQuMS4yMTAwOC4xMDguNjMuMS4zBAZibG9nZ3M="),
          new SessionTrackingControl("192.0.2.1", "app.example.com", "1.3.6.1.4.1.21008.108.63.1.3", "bloggs"),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  session tracking control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request-response")
  public void encode(final byte[] berValue, final SessionTrackingControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  session tracking control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request-response")
  public void decode(final byte[] berValue, final SessionTrackingControl expected)
    throws Exception
  {
    final SessionTrackingControl actual = new SessionTrackingControl();
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
