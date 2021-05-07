/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SyncReplCookie}.
 *
 * @author  Middleware Services
 */
public class SyncReplCookieTest
{


  /**
   * Sync repl cookie test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "cookie")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          "rid=000,csn=20210506131729.859082Z#000000#000#000000",
          "000",
          "20210506131729.859082Z",
          "000000",
          "000",
          "000000",
        },
      };
  }


  /**
   * @param  cookie  to parse.
   * @param  rid  rid value.
   * @param  csnTime  csn time.
   * @param  csnCount  csn count.
   * @param  csnSid  csn sid.
   * @param  csnMod  csn mod.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "cookie")
  public void parse(
    final String cookie,
    final String rid,
    final String csnTime,
    final String csnCount,
    final String csnSid,
    final String csnMod)
    throws Exception
  {
    final SyncReplCookie syncCookie = new SyncReplCookie(cookie);
    Assert.assertEquals(rid, syncCookie.getRid());
    Assert.assertEquals(csnTime, syncCookie.getCsn().getTime());
    Assert.assertEquals(csnCount, syncCookie.getCsn().getCount());
    Assert.assertEquals(csnSid, syncCookie.getCsn().getSid());
    Assert.assertEquals(csnMod, syncCookie.getCsn().getMod());
  }
}
