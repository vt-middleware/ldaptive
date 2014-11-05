/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SortRequestControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SortRequestControlTest
{


  /**
   * Sort request control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // sort on createTimestamp
        // BER: 30:13:30:11:04:0F:63:72:65:61:74:65:54:69:6D:
        // 65:73:74:61:6D:70
        new Object[] {
          LdapUtils.base64Decode("MBMwEQQPY3JlYXRlVGltZXN0YW1w"),
          new SortRequestControl(
            new SortKey[] {new SortKey("createTimestamp")},
            true),
        },
        // sort on uugid
        // BER: 30:19:30:17:04:05:75:75:67:69:64:80:0E:63:61:73:
        // 65:45:78:61:63:74:4D:61:74:63:68
        new Object[] {
          LdapUtils.base64Decode("MBkwFwQFdXVnaWSADmNhc2VFeGFjdE1hdGNo"),
          new SortRequestControl(
            new SortKey[] {new SortKey("uugid", "caseExactMatch")},
            true),
        },
        // sort on uid, reverse order
        // BER: 30:18:30:16:04:03:75:69:64:80:0C:69:6E:74:65:67:
        // 65:72:4D:61:74:63:68:81:01:FF
        new Object[] {
          LdapUtils.base64Decode("MBgwFgQDdWlkgAxpbnRlZ2VyTWF0Y2iBAf8="),
          new SortRequestControl(
            new SortKey[] {new SortKey("uid", "integerMatch", true)},
            true),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  sort request control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "request"
  )
  public void encode(final byte[] berValue, final SortRequestControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
