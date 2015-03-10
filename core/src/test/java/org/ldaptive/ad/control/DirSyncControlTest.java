/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DirSyncControl}.
 *
 * @author  Middleware Services
 */
public class DirSyncControlTest
{


  /**
   * Dir sync request control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request-response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // flags=0, maxAttrCount=0, no cookie
        // BER: 30:08:02:01:00:02:01:00:04:00
        new Object[] {
          LdapUtils.base64Decode("MAgCAQACAQAEAA=="),
          new DirSyncControl(),
        },
        // flags=0, maxAttrCount=0, cookie
        new Object[] {
          LdapUtils.base64Decode(
            "MHQCAQACAQAEbE1TRFMDAAAAAmDMQ7HCzQEAAAAAAAAAACgAAABbzwEAAAAAAAAA" +
            "AAAAAAAAW88BAAAAAADzox7OKwdpRIu4ZIWpCoubAQAAAAAAAAABAAAAAAAAAPOj" +
            "Hs4rB2lEi7hkhakKi5vyzwEAAAAAAA=="),
          new DirSyncControl(
            null,
            new byte[] {
              (byte) 0x4D, (byte) 0x53, (byte) 0x44, (byte) 0x53, (byte) 0x03,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60,
              (byte) 0xCC, (byte) 0x43, (byte) 0xB1, (byte) 0xC2, (byte) 0xCD,
              (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x28,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5B, (byte) 0xCF,
              (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5B,
              (byte) 0xCF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0xF3, (byte) 0xA3, (byte) 0x1E,
              (byte) 0xCE, (byte) 0x2B, (byte) 0x07, (byte) 0x69, (byte) 0x44,
              (byte) 0x8B, (byte) 0xB8, (byte) 0x64, (byte) 0x85, (byte) 0xA9,
              (byte) 0x0A, (byte) 0x8B, (byte) 0x9B, (byte) 0x01, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF3,
              (byte) 0xA3, (byte) 0x1E, (byte) 0xCE, (byte) 0x2B, (byte) 0x07,
              (byte) 0x69, (byte) 0x44, (byte) 0x8B, (byte) 0xB8, (byte) 0x64,
              (byte) 0x85, (byte) 0xA9, (byte) 0x0A, (byte) 0x8B, (byte) 0x9B,
              (byte) 0xF2, (byte) 0xCF, (byte) 0x01, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00,
            },
            true),
        },
        // flags=ANCESTORS_FIRST_ORDER, maxAttrCount=100, no cookie
        // BER: 30:09:02:02:08:00:02:01:64:04:00:
        new Object[] {
          LdapUtils.base64Decode("MAkCAggAAgFkBAA="),
          new DirSyncControl(
            new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, },
            null,
            100,
            false),
        },
        // flags=ANCESTORS_FIRST_ORDER, maxAttrCount=100, cookie
        new Object[] {
          LdapUtils.base64Decode(
            "MHUCAggAAgFkBGxNU0RTAwAAAN2j6LS3ws0BAAAAAAAAAAAoAAAAW88BAAAAAAAA" +
            "AAAAAAAAAFvPAQAAAAAA86MezisHaUSLuGSFqQqLmwEAAAAAAAAAAQAAAAAAAADz" +
            "ox7OKwdpRIu4ZIWpCoubCdABAAAAAAA="),
          new DirSyncControl(
            new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, },
            new byte[] {
              (byte) 0x4D, (byte) 0x53, (byte) 0x44, (byte) 0x53, (byte) 0x03,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xDD, (byte) 0xA3,
              (byte) 0xE8, (byte) 0xB4, (byte) 0xB7, (byte) 0xC2, (byte) 0xCD,
              (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x28,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5B, (byte) 0xCF,
              (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5B,
              (byte) 0xCF, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0xF3, (byte) 0xA3, (byte) 0x1E,
              (byte) 0xCE, (byte) 0x2B, (byte) 0x07, (byte) 0x69, (byte) 0x44,
              (byte) 0x8B, (byte) 0xB8, (byte) 0x64, (byte) 0x85, (byte) 0xA9,
              (byte) 0x0A, (byte) 0x8B, (byte) 0x9B, (byte) 0x01, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xF3,
              (byte) 0xA3, (byte) 0x1E, (byte) 0xCE, (byte) 0x2B, (byte) 0x07,
              (byte) 0x69, (byte) 0x44, (byte) 0x8B, (byte) 0xB8, (byte) 0x64,
              (byte) 0x85, (byte) 0xA9, (byte) 0x0A, (byte) 0x8B, (byte) 0x9B,
              (byte) 0x09, (byte) 0xD0, (byte) 0x01, (byte) 0x00, (byte) 0x00,
              (byte) 0x00, (byte) 0x00, (byte) 0x00,
            },
            100,
            true),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  dir sync control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request-response")
  public void encode(final byte[] berValue, final DirSyncControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  dir sync control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"control"}, dataProvider = "request-response")
  public void decode(final byte[] berValue, final DirSyncControl expected)
    throws Exception
  {
    final DirSyncControl actual = new DirSyncControl(expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
