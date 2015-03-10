/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link VirtualListViewResponseControl}.
 *
 * @author  Middleware Services
 */
public class VirtualListViewResponseControlTest
{


  /**
   * Virtual list view response control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // position=1, count=59, result=success, context not null
        // BER: 30:13:02:01:01:02:01:3B:02:01:00:04:08:80:28:7D:08:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBMCAQECATsCAQAECIAofQgAAAAA"),
          new VirtualListViewResponseControl(
            1,
            59,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x28,
              (byte) 0x7D,
              (byte) 0x08,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=10, count=55, result=success, context not null
        // BER: 30:13:02:01:0A:02:01:37:02:01:00:04:08:00:9A:96:01:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBMCAQoCATcCAQAECACalgEAAAAA"),
          new VirtualListViewResponseControl(
            10,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x00,
              (byte) 0x9A,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=12, count=55, result=success, context not null
        // BER: 30:13:02:01:0C:02:01:37:02:01:00:04:08:80:99:96:01:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBMCAQwCATcCAQAECICZlgEAAAAA"),
          new VirtualListViewResponseControl(
            12,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x99,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
        // position=22, count=55, result=success, context not null
        // BER: 30:13:02:01:16:02:01:37:02:01:00:04:08:80:99:96:01:00:00:00:00
        new Object[] {
          LdapUtils.base64Decode("MBMCARYCATcCAQAECICZlgEAAAAA"),
          new VirtualListViewResponseControl(
            22,
            55,
            ResultCode.SUCCESS,
            new byte[] {
              (byte) 0x80,
              (byte) 0x99,
              (byte) 0x96,
              (byte) 0x01,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0x00,
            }),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  virtual list view response control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "response")
  public void decode(final byte[] berValue, final VirtualListViewResponseControl expected)
    throws Exception
  {
    final VirtualListViewResponseControl actual = new VirtualListViewResponseControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
