/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link VirtualListViewRequestControl}.
 *
 * @author  Middleware Services
 */
public class VirtualListViewRequestControlTest
{


  /**
   * Virtual list view request control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // before=0, after=9, offset=1, count=0, no cookie
        // BER: 30:0e:02:01:00:02:01:09:a0:06:02:01:01:02:01:00
        new Object[] {
          LdapUtils.base64Decode("MA4CAQACAQmgBgIBAQIBAA=="),
          new VirtualListViewRequestControl(1, 0, 9, true),
        },
        // before=0, after=9, offset=11, count=59, with cookie
        // BER: 30:18:02:01:00:02:01:09:a0:06:02:01:0b:02:01:3b:
        // 04:08:00:9a:96:01:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBgCAQACAQmgBgIBCwIBOwQIAJqWAQAAAAA="),
          new VirtualListViewRequestControl(
            11,
            0,
            9,
            59,
            new byte[] {
              (byte) 0x00,
              (byte) 0x9a,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            },

            true),
        },
        // before=0, after=9, assertion=549810, no cookie
        // BER: 30:0e:02:01:00:02:01:09:81:06:35:34:39:38:31:30
        new Object[] {
          LdapUtils.base64Decode("MA4CAQACAQmBBjU0OTgxMA=="),
          new VirtualListViewRequestControl("549810", 0, 9, true),
        },
        // before=0, after=9, assertion=549820, with cookie
        // BER: 30:18:02:01:00:02:01:09:81:06:35:34:39:38:32:30:
        // 04:08:80:99:96:01:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBgCAQACAQmBBjU0OTgyMAQIgJmWAQAAAAA="),
          new VirtualListViewRequestControl(
            "549820",
            0,
            9,
            new byte[] {
              (byte) 0x80,
              (byte) 0x99,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            },

            true),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  virtual list view request control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request")
  public void encode(final byte[] berValue, final VirtualListViewRequestControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
