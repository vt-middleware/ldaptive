/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncDoneControl}.
 *
 * @author  Middleware Services
 */
public class SyncDoneControlTest
{


  /**
   * Sync done control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // 52 length cookie, refreshDeletes true
        // BER:30:39:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:37
        // :30:33:32:30:34:38:30:30:2E:36:30:39:37:32:31:5A:23:30:30:30:30:30
        // :30:23:30:30:30:23:30:30:30:30:30:30:01:01:FF
        new Object[] {
          LdapUtils.base64Decode(
            "MDkENHJpZD0wMDAsY3NuPTIwMTIwNzAzMjA0ODAwLjYwOTcyMVojMDAwMDAwIzAw" +
            "MCMwMDAwMDABAf8="),
          new SyncDoneControl(
            new byte[] {
              (byte) 0x72, (byte) 0x69, (byte) 0x64, (byte) 0x3D, (byte) 0x30,
              (byte) 0x30, (byte) 0x30, (byte) 0x2C, (byte) 0x63, (byte) 0x73,
              (byte) 0x6E, (byte) 0x3D, (byte) 0x32, (byte) 0x30, (byte) 0x31,
              (byte) 0x32, (byte) 0x30, (byte) 0x37, (byte) 0x30, (byte) 0x33,
              (byte) 0x32, (byte) 0x30, (byte) 0x34, (byte) 0x38, (byte) 0x30,
              (byte) 0x30, (byte) 0x2E, (byte) 0x36, (byte) 0x30, (byte) 0x39,
              (byte) 0x37, (byte) 0x32, (byte) 0x31, (byte) 0x5A, (byte) 0x23,
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
   * @param  expected  sync done control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "response")
  public void decode(final byte[] berValue, final SyncDoneControl expected)
    throws Exception
  {
    final SyncDoneControl actual = new SyncDoneControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
