/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
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
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x30, 0x39, 0x04, 0x34, 0x72, 0x69, 0x64, 0x3D, 0x30, 0x30, 0x30, 0x2C, 0x63, 0x73, 0x6E, 0x3D, 0x32,
              0x30, 0x31, 0x32, 0x30, 0x37, 0x30, 0x33, 0x32, 0x30, 0x34, 0x38, 0x30, 0x30, 0x2E, 0x36, 0x30, 0x39,
              0x37, 0x32, 0x31, 0x5A, 0x23, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x23, 0x30, 0x30, 0x30, 0x23, 0x30,
              0x30, 0x30, 0x30, 0x30, 0x30, 0x01, 0x01, (byte) 0xFF}),
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
  public void decode(final DERBuffer berValue, final SyncDoneControl expected)
    throws Exception
  {
    final SyncDoneControl actual = new SyncDoneControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
