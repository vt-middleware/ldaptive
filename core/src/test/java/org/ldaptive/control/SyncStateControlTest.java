/*
  $Id: SyncStateControlTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.control;

import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncStateControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SyncStateControlTest
{


  /**
   * Sync state control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // Add state, no cookie
        // BER:30:15:0A:01:01:04:10:84:31:77:EC:5B:0E:10:31:82:7F:11:6F:F5:6E:4E
        //    :59
        new Object[] {
          LdapUtils.base64Decode("MBUKAQEEEIQxd+xbDhAxgn8Rb/VuTlk="),
          new SyncStateControl(
            SyncStateControl.State.ADD,
            UUID.fromString("843177ec-5b0e-1031-827f-116ff56e4e59"),
            null,
            false),
        },
        // Modify state, cookie of length 52
        // BER:30:4B:0A:01:02:04:10:5D:5D:A5:D0:5B:E2:10:31:82:84:11:6F:F5:6E:4E
        //    :59:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:37:30
        //    :36:31:38:31:35:35:32:2E:33:33:37:37:31:38:5A:23:30:30:30:30:30:30
        //    :23:30:30:30:23:30:30:30:30:30:30:
        new Object[] {
          LdapUtils.base64Decode(
            "MEsKAQIEEF1dpdBb4hAxgoQRb/VuTlkENHJpZD0wMDAsY3NuPTIwMTIwNzA2MTgx" +
            "NTUyLjMzNzcxOFojMDAwMDAwIzAwMCMwMDAwMDA="),
          new SyncStateControl(
            SyncStateControl.State.MODIFY,
            UUID.fromString("5d5da5d0-5be2-1031-8284-116ff56e4e59"),
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
            false),
        },
        // Add state, cookie length of 52, empty UUID
        // BER:30:3B:0A:01:01:04:00:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32
        //    :30:31:33:30:32:31:35:32:31:32:33:32:30:2E:34:36:34:34:38:35:5A:23
        //    :30:30:30:30:30:30:23:30:30:30:23:30:30:30:30:30:30:
        new Object[] {
          LdapUtils.base64Decode(
            "MDsKAQEEAAQ0cmlkPTAwMCxjc249MjAxMzAyMTUyMTIzMjAuNDY0NDg1WiMwMDAw" +
            "MDAjMDAwIzAwMDAwMA=="),
          new SyncStateControl(
            SyncStateControl.State.ADD,
            null,
            new byte[] {
              (byte) 0x72, (byte) 0x69, (byte) 0x64, (byte) 0x3d, (byte) 0x30,
              (byte) 0x30, (byte) 0x30, (byte) 0x2c, (byte) 0x63, (byte) 0x73,
              (byte) 0x6e, (byte) 0x3d, (byte) 0x32, (byte) 0x30, (byte) 0x31,
              (byte) 0x33, (byte) 0x30, (byte) 0x32, (byte) 0x31, (byte) 0x35,
              (byte) 0x32, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x32,
              (byte) 0x30, (byte) 0x2e, (byte) 0x34, (byte) 0x36, (byte) 0x34,
              (byte) 0x34, (byte) 0x38, (byte) 0x35, (byte) 0x5a, (byte) 0x23,
              (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x30, (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
              (byte) 0x30, (byte) 0x30,
            },
            false),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  sync state control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "response"
  )
  public void decode(final byte[] berValue, final SyncStateControl expected)
    throws Exception
  {
    final SyncStateControl actual = new SyncStateControl(
      expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
