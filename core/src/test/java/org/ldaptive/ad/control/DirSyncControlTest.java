/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x08, 0x02, 0x01, 0x00, 0x02, 0x01, 0x00, 0x04, 0x00}),
          new DirSyncControl(),
        },
        // flags=0, maxAttrCount=0, cookie
        new Object[] {
          new DefaultDERBuffer(
            LdapUtils.base64Decode(
              "MHQCAQACAQAEbE1TRFMDAAAAAmDMQ7HCzQEAAAAAAAAAACgAAABbzwEAAAAAAAAA" +
              "AAAAAAAAW88BAAAAAADzox7OKwdpRIu4ZIWpCoubAQAAAAAAAAABAAAAAAAAAPOj" +
              "Hs4rB2lEi7hkhakKi5vyzwEAAAAAAA==")),
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
        new Object[] {
          new DefaultDERBuffer(new byte[] {0x30, 0x09, 0x02, 0x02, 0x08, 0x00, 0x02, 0x01, 0x64, 0x04, 0x00}),
          new DirSyncControl(
            new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, },
            null,
            100,
            false),
        },
        // flags=ANCESTORS_FIRST_ORDER, maxAttrCount=100, cookie
        new Object[] {
          new DefaultDERBuffer(
            LdapUtils.base64Decode(
              "MHUCAggAAgFkBGxNU0RTAwAAAN2j6LS3ws0BAAAAAAAAAAAoAAAAW88BAAAAAAAA" +
              "AAAAAAAAAFvPAQAAAAAA86MezisHaUSLuGSFqQqLmwEAAAAAAAAAAQAAAAAAAADz" +
              "ox7OKwdpRIu4ZIWpCoubCdABAAAAAAA=")),
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
   * @param  control  dir sync control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request-response")
  public void encode(final DERBuffer berValue, final DirSyncControl control)
    throws Exception
  {
    assertThat(control.encode()).isEqualTo(berValue.getRemainingBytes());
  }


  /**
   * @param  berValue  to decode.
   * @param  control  dir sync control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request-response")
  public void decode(final DERBuffer berValue, final DirSyncControl control)
    throws Exception
  {
    final DirSyncControl actual = new DirSyncControl(control.getCriticality());
    actual.decode(berValue);
    assertThat(actual).isEqualTo(control);
  }
}
