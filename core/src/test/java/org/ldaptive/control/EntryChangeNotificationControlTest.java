/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
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
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x03, 0x0A, 0x01, 0x04}),
          new EntryChangeNotificationControl(PersistentSearchChangeType.MODIFY),
        },
        // changeType=ModDn, previousDn=uid=1,ou=test,dc=ldaptive,dc=org
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x25, 0x0A, 0x01, 0x08, 0x04, 0x20, 0x75, 0x69, 0x64, 0x3D, 0x31, 0x2C, 0x6F, 0x75, 0x3D, 0x74,
              0x65, 0x73, 0x74, 0x2C, 0x64, 0x63, 0x3D, 0x6C, 0x64, 0x61, 0x70, 0x74, 0x69, 0x76, 0x65, 0x2C, 0x64,
              0x63, 0x3D, 0x6F, 0x72, 0x67}),
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
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final EntryChangeNotificationControl expected)
    throws Exception
  {
    final EntryChangeNotificationControl actual = new EntryChangeNotificationControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
