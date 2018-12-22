/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link GreaterOrEqualFilter}.
 *
 * @author  Middleware Services
 */
public class GreaterOrEqualFilterTest
{


  /**
   * Greater or equal test data.
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
          "createTimestamp>=",
          null,
        },
        new Object[] {
          ">=",
          null,
        },
        new Object[] {
          "createTimestamp>=20170102030405.678Z",
          null,
        },
        new Object[] {
          "(>=20170102030405.678Z)",
          null,
        },
        new Object[] {
          "(createTimestamp>=20170102030405.678Z",
          null,
        },
        new Object[] {
          "createTimestamp>=20170102030405.678Z)",
          null,
        },
        new Object[] {
          "(createTimestamp>=)",
          new GreaterOrEqualFilter("createTimestamp", ""),
        },
        new Object[] {
          "(createTimestamp>=20170102030405.678Z)",
          new GreaterOrEqualFilter("createTimestamp", "20170102030405.678Z"),
        },
      };
  }


  /**
   * @param  value  to parse.
   * @param  filter  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "filter")
  public void parse(final String value, final GreaterOrEqualFilter filter)
    throws Exception
  {
    Assert.assertEquals(GreaterOrEqualFilter.parse(value), filter);
  }


  /**
   * Greater or equal test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new GreaterOrEqualFilter("createTimestamp", "20170102030405.678Z"),
          new byte[] {
            (byte) 0xa5, 0x26,
            0x04, 0x0f, 0x63, 0x72, 0x65, 0x61, 0x74, 0x65, 0x54, 0x69, 0x6d, 0x65, 0x73, 0x74, 0x61, 0x6d, 0x70,
            0x04, 0x13, 0x32, 0x30, 0x31, 0x37, 0x30, 0x31, 0x30, 0x32, 0x30, 0x33, 0x30, 0x34, 0x30, 0x35, 0x2e, 0x36,
            0x37, 0x38, 0x5a, },
        },
      };
  }


  /**
   * @param  filter  to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "component")
  public void encode(final GreaterOrEqualFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
