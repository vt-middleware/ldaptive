/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ApproximateFilter}.
 *
 * @author  Middleware Services
 */
public class ApproximateFilterTest
{


  /**
   * Approximate test data.
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
          "givenName~=",
          null,
        },
        new Object[] {
          "~=",
          null,
        },
        new Object[] {
          "givenName~=John",
          null,
        },
        new Object[] {
          "(~=John)",
          null,
        },
        new Object[] {
          "(givenName~=John",
          null,
        },
        new Object[] {
          "givenName~=John)",
          null,
        },
        new Object[] {
          "(givenName~=)",
          new ApproximateFilter("givenName", ""),
        },
        new Object[] {
          "(givenName~=John)",
          new ApproximateFilter("givenName", "John"),
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
  public void parseRegex(final String value, final ApproximateFilter filter)
    throws Exception
  {
    Assert.assertEquals(RegexFilterFunction.parseApproximateFilter(value), filter);
  }


  /**
   * Approximate test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new ApproximateFilter("givenName", "John"),
          new byte[] {
            (byte) 0xa8, 0x11,
            0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6e, 0x4e, 0x61, 0x6d, 0x65,
            0x04, 0x04, 0x4a, 0x6f, 0x68, 0x6e, },
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
  public void encode(final ApproximateFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
