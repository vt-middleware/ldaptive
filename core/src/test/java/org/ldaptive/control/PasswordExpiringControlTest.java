/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PasswordExpiringControl}.
 *
 * @author  Middleware Services
 */
public class PasswordExpiringControlTest
{


  /**
   * Password expiring control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x34, 0x36, 0x31}),
          new PasswordExpiringControl(461),
        },
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x33, 0x38, 0x34}),
          new PasswordExpiringControl(384),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  password expiring control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "response")
  public void decode(final DERBuffer berValue, final PasswordExpiringControl expected)
    throws Exception
  {
    final PasswordExpiringControl actual = new PasswordExpiringControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
