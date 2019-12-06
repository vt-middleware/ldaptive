/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LessOrEqualFilter}.
 *
 * @author  Middleware Services
 */
public class LessOrEqualFilterTest
{


  /**
   * Less or equal test data.
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
          "accountBalance<=",
          null,
        },
        new Object[] {
          "<=",
          null,
        },
        new Object[] {
          "accountBalance<=1234",
          null,
        },
        new Object[] {
          "(<=1234)",
          null,
        },
        new Object[] {
          "(accountBalance<=1234",
          null,
        },
        new Object[] {
          "accountBalance<=1234)",
          null,
        },
        new Object[] {
          "(accountBalance<=)",
          new LessOrEqualFilter("accountBalance", ""),
        },
        new Object[] {
          "(accountBalance<=1234)",
          new LessOrEqualFilter("accountBalance", "1234"),
        },
      };
  }


  /**
   * @param  value  to parse.
   * @param  filter  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "filter")
  public void parseRegex(final String value, final LessOrEqualFilter filter)
    throws Exception
  {
    Assert.assertEquals(RegexFilterFunction.parseLessOrEqualFilter(value), filter);
  }


  /**
   * Less or equal test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new LessOrEqualFilter("accountBalance", "1234"),
          new byte[] {
            (byte) 0xa6, 0x16,
            0x04, 0x0e, 0x61, 0x63, 0x63, 0x6f, 0x75, 0x6e, 0x74, 0x42, 0x61, 0x6c, 0x61, 0x6e, 0x63, 0x65,
            0x04, 0x04, 0x31, 0x32, 0x33, 0x34, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "component")
  public void encode(final LessOrEqualFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
