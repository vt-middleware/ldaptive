/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link VerifyNameControl}.
 *
 * @author  Middleware Services
 */
public class VerifyNameControlTest
{


  /**
   * Verify name control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // BER:
        // 30:14:02:01:00:04:0F:61:64:2E:6C:64:61:70:74:69:76:65:2E:6F:72:67
        new Object[] {
          LdapUtils.base64Decode("MBQCAQAED2FkLmxkYXB0aXZlLm9yZw=="),
          new VerifyNameControl("ad.ldaptive.org"),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  verify name control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request")
  public void encode(final byte[] berValue, final VerifyNameControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
