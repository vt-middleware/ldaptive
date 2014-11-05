/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncRequestControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SyncRequestControlTest
{


  /**
   * Sync request control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // refresh only, reloadHint false
        // BER:30:06:02:01:01:01:01:00
        new Object[] {
          LdapUtils.base64Decode("MAYCAQEBAQA="),
          new SyncRequestControl(SyncRequestControl.Mode.REFRESH_ONLY, true),
        },
        // refresh and persist with cookie, reloadHint true
        // BER:30:30:3C:0A:01:03:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:
        //     30:31:32:30:37:30:36:31:38:31:35:35:32:2E:33:33:37:37:31:38:5A:
        //     23:30:30:30:30:30:30:23:30:30:30:23:30:30:30:30:30:30:01:01:FF:
        new Object[] {
          LdapUtils.base64Decode(
            "MDwCAQMENHJpZD0wMDAsY3NuPTIwMTIwNzA2MTgxNTUyLjMzNzcxOFojMDAwMDAw" +
            "IzAwMCMwMDAwMDABAf8="),
          new SyncRequestControl(
            SyncRequestControl.Mode.REFRESH_AND_PERSIST,
            new byte[] {
              (byte) 0x72, (byte) 0x69, (byte) 0x64, (byte) 0x3D, (byte) 0x30,
              (byte) 0x30, (byte) 0x30, (byte) 0x2C, (byte) 0x63, (byte) 0x73,
              (byte) 0x6E, (byte) 0x3D, (byte) 0x32, (byte) 0x30, (byte) 0x31,
              (byte) 0x32, (byte) 0x30, (byte) 0x37, (byte) 0x30, (byte) 0x36,
              (byte) 0x31, (byte) 0x38, (byte) 0x31, (byte) 0x35, (byte) 0x35,
              (byte) 0x32, (byte) 0x2E, (byte) 0x33, (byte) 0x33, (byte) 0x37,
              (byte) 0x37, (byte) 0x31, (byte) 0x38, (byte) 0x5A, (byte) 0x23,
              (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x30, (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x30, (byte) 0x30,
            },
            true,
            true),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  sync request control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "request"
  )
  public void decode(final byte[] berValue, final SyncRequestControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
