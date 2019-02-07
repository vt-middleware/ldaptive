/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PresenceFilter}.
 *
 * @author  Middleware Services
 */
public class PresenceFilterTest
{


  /**
   * Presence test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "filter")
  public Object[][] createFilter()
  {
    return
      new Object[][] {
        new Object[] {
          "",
          null,
        },
        new Object[] {
          "attr=",
          null,
        },
        new Object[] {
          "=*",
          null,
        },
        new Object[] {
          "attr=*",
          null,
        },
        new Object[] {
          "(attr=*",
          null,
        },
        new Object[] {
          "attr=*)",
          null,
        },
        new Object[] {
          "(attr=)",
          null,
        },
        new Object[] {
          "(=*)",
          null,
        },
        new Object[] {
          "(attr=*)",
          new PresenceFilter("attr"),
        },
      };
  }


  /**
   * @param  value  to parse.
   * @param  filter  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "provider", dataProvider = "filter")
  public void parse(final String value, final PresenceFilter filter)
    throws Exception
  {
    Assert.assertEquals(PresenceFilter.parse(value), filter);
  }


  /**
   * Presence test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "encoded")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new PresenceFilter("uid"),
          new byte[] {
            (byte) 0x87, 0x03, 0x75, 0x69, 0x64, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "provider", dataProvider = "encoded")
  public void encode(final PresenceFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
