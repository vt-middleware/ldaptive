/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DnsSrvConnectionStrategy}.
 *
 * @author  Middleware Services
 */
public class DnsSrvConnectionStrategyTest
{

  /** Strategy to test. */
  private final DnsSrvConnectionStrategy strategy = new DnsSrvConnectionStrategy();


  /**
   * DNS test data.
   *
   * @return  test data
   */
  @DataProvider(name = "records")
  public Object[][] createRecords()
  {
    return
      new Object[][] {
        new Object[] {
          new DnsSrvConnectionStrategy.SrvRecord[] {
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 larry.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 curly.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 moe.ldaptive.org.", 0),
          },
          new DnsSrvConnectionStrategy.SrvRecord[] {
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 larry.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 curly.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("0 0 389 moe.ldaptive.org.", 0),
          },
        },
        new Object[] {
          new DnsSrvConnectionStrategy.SrvRecord[] {
            new DnsSrvConnectionStrategy.SrvRecord("5 100 389 larry.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("1 0 389 curly.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("3 200 389 moe.ldaptive.org.", 0),
          },
          new DnsSrvConnectionStrategy.SrvRecord[] {
            new DnsSrvConnectionStrategy.SrvRecord("1 0 389 curly.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("3 200 389 moe.ldaptive.org.", 0),
            new DnsSrvConnectionStrategy.SrvRecord("5 100 389 larry.ldaptive.org.", 0),
          },
        },
      };
  }


  /**
   * @param  records  to sort
   * @param  sorted  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "records")
  public void sortSrvRecords(
    final DnsSrvConnectionStrategy.SrvRecord[] records,
    final DnsSrvConnectionStrategy.SrvRecord[] sorted)
    throws Exception
  {
    Assert.assertEquals(
      strategy.sortSrvRecords(Arrays.asList(records)).toArray(new DnsSrvConnectionStrategy.SrvRecord[records.length]),
      sorted);
  }
}
