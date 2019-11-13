/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.intermediate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncInfoMessage}.
 *
 * @author  Middleware Services
 */
public class SyncInfoMessageTest
{


  /**
   * Sync state control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    final byte[] cookie = new byte[] {
      (byte) 0x72, (byte) 0x69, (byte) 0x64, (byte) 0x3D, (byte) 0x30,
      (byte) 0x30, (byte) 0x30, (byte) 0x2C, (byte) 0x63, (byte) 0x73,
      (byte) 0x6E, (byte) 0x3D, (byte) 0x32, (byte) 0x30, (byte) 0x31,
      (byte) 0x32, (byte) 0x30, (byte) 0x37, (byte) 0x30, (byte) 0x39,
      (byte) 0x32, (byte) 0x30, (byte) 0x31, (byte) 0x33, (byte) 0x31,
      (byte) 0x39, (byte) 0x2E, (byte) 0x37, (byte) 0x36, (byte) 0x34,
      (byte) 0x39, (byte) 0x31, (byte) 0x35, (byte) 0x5A, (byte) 0x23,
      (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
      (byte) 0x30, (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30,
      (byte) 0x23, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
      (byte) 0x30, (byte) 0x30,
    };
    final SyncInfoMessage m1 = new SyncInfoMessage();
    m1.setMessageType(SyncInfoMessage.Type.REFRESH_DELETE);
    m1.setCookie(cookie);

    final SyncInfoMessage m2 = new SyncInfoMessage();
    m2.setMessageType(SyncInfoMessage.Type.NEW_COOKIE);
    m2.setCookie(cookie);

    final SyncInfoMessage m3 = new SyncInfoMessage();
    m3.setMessageType(SyncInfoMessage.Type.REFRESH_PRESENT);
    m3.setCookie(cookie);
    m3.setRefreshDone(false);

    final Set<UUID> uuids = new HashSet<>();
    uuids.add(UUID.fromString("5d5da5d0-5be2-1031-8284-116ff56e4e59"));
    uuids.add(UUID.fromString("843177ec-5b0e-1031-827f-116ff56e4e59"));

    final SyncInfoMessage m4 = new SyncInfoMessage();
    m4.setMessageType(SyncInfoMessage.Type.SYNC_ID_SET);
    m4.setCookie(cookie);
    m4.setEntryUuids(uuids);
    m4.setRefreshDeletes(true);
    return
      new Object[][] {
        // refresh delete with cookie
        // BER:A1:36:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:
        // 37:30:39:32:30:31:33:31:39:2E:37:36:34:39:31:35:5A:23:30:30:30:
        // 30:30:30:23:30:30:30:23:30:30:30:30:30:30
        new Object[] {
          LdapUtils.base64Decode("oTYENHJpZD0wMDAsY3NuPTIwMTIwNzA5MjAxMzE5Ljc2NDkxNVojMDAwMDAwIzAw" +
            "MCMwMDAwMDA="),
          m1,
        },
        // new cookie
        // BER:80:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:37:30:
        // 39:32:30:31:33:31:39:2E:37:36:34:39:31:35:5A:23:30:30:30:30:30:
        // 30:23:30:30:30:23:30:30:30:30:30:30:
        new Object[] {
          LdapUtils.base64Decode("gDRyaWQ9MDAwLGNzbj0yMDEyMDcwOTIwMTMxOS43NjQ5MTVaIzAwMDAwMCMwMDAj" +
            "MDAwMDAw"),
          m2,
        },
        // refresh present with cookie, refresh done false
        // BER:A2:39:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:
        // 37:30:39:32:30:31:33:31:39:2E:37:36:34:39:31:35:5A:23:30:30:30:
        // 30:30:30:23:30:30:30:23:30:30:30:30:30:30:01:01:00:
        new Object[] {
          LdapUtils.base64Decode(
            "ojkENHJpZD0wMDAsY3NuPTIwMTIwNzA5MjAxMzE5Ljc2NDkxNVojMDAwMDAwIzAw" +
            "MCMwMDAwMDABAQA="),
          m3,
        },
        // sync id set, refresh deletes true
        // BER:A3:5F:04:34:72:69:64:3D:30:30:30:2C:63:73:6E:3D:32:30:31:32:30:
        // 37:30:39:32:30:31:33:31:39:2E:37:36:34:39:31:35:5A:23:30:30:30:
        // 30:30:30:23:30:30:30:23:30:30:30:30:30:30:01:01:FF:31:24:04:10:
        // 5D:5D:A5:D0:5B:E2:10:31:82:84:11:6F:F5:6E:4E:59:04:10:84:31:77:
        // EC:5B:0E:10:31:82:7F:11:6F:F5:6E:4E:59:
        new Object[] {
          LdapUtils.base64Decode(
            "o18ENHJpZD0wMDAsY3NuPTIwMTIwNzA5MjAxMzE5Ljc2NDkxNVojMDAwMDAwIzAw" +
            "MCMwMDAwMDABAf8xJAQQXV2l0FviEDGChBFv9W5OWQQQhDF37FsOEDGCfxFv9W5O" +
            "WQ=="),
          m4,
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  sync info message to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "intermediate", dataProvider = "response")
  public void decode(final byte[] berValue, final SyncInfoMessage expected)
    throws Exception
  {
    final SyncInfoMessage actual = new SyncInfoMessage();
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
