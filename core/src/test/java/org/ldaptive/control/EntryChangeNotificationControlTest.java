/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link EntryChangeNotificationControl}.
 *
 * @author  Middleware Services
 */
public class EntryChangeNotificationControlTest
{


  /**
   * Entry change notification control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // changeType=Modify
        // BER: 30:03:0A:01:04
        new Object[] {
          LdapUtils.base64Decode("MAMKAQQ="),
          new EntryChangeNotificationControl(PersistentSearchChangeType.MODIFY),
        },
        // changeType=ModDn, previousDn=uid=1,ou=test,dc=ldaptive,dc=org
        // BER:
        // 30:25:0A:01:08:04:20:75:69:64:3D:31:2C:6F:75:3D:74:65:73:74:2C:64:
        // 63:3D:6C:64:61:70:74:69:76:65:2C:64:63:3D:6F:72:67
        new Object[] {
          LdapUtils.base64Decode("MCUKAQgEIHVpZD0xLG91PXRlc3QsZGM9bGRhcHRpdmUsZGM9b3Jn"),
          new EntryChangeNotificationControl(PersistentSearchChangeType.MODDN, "uid=1,ou=test,dc=ldaptive,dc=org", -1),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  entry change notification control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "response")
  public void decode(final byte[] berValue, final EntryChangeNotificationControl expected)
    throws Exception
  {
    final EntryChangeNotificationControl actual = new EntryChangeNotificationControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
