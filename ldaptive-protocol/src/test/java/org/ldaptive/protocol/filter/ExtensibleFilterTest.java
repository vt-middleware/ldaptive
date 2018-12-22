/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ExtensibleFilter}.
 *
 * @author  Middleware Services
 */
public class ExtensibleFilterTest
{


  /**
   * Extensible test data.
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
          "givenName:=",
          null,
        },
        new Object[] {
          ":=",
          null,
        },
        new Object[] {
          "givenName:=John",
          null,
        },
        new Object[] {
          "(=John)",
          null,
        },
        new Object[] {
          "(givenName:=John",
          null,
        },
        new Object[] {
          "givenName:=John)",
          null,
        },
        new Object[] {
          "(givenName:=)",
          new ExtensibleFilter(null, "givenName", ""),
        },
        new Object[] {
          "(givenName:=John)",
          new ExtensibleFilter(null, "givenName", "John"),
        },
        new Object[] {
          "(givenName:dn:=John)",
          new ExtensibleFilter(null, "givenName", "John", true),
        },
        new Object[] {
          "(givenName:caseExactMatch:=John)",
          new ExtensibleFilter("caseExactMatch", "givenName", "John"),
        },
        new Object[] {
          "(givenName:dn:2.5.13.5:=John)",
          new ExtensibleFilter("2.5.13.5", "givenName", "John", true),
        },
        new Object[] {
          "(:caseExactMatch:=John)",
          new ExtensibleFilter("caseExactMatch", null, "John"),
        },
        new Object[] {
          "(:dn:2.5.13.5:=John)",
          new ExtensibleFilter("2.5.13.5", null, "John", true),
        },
        new Object[] {
          "(uid:=jdoe)",
          new ExtensibleFilter(null, "uid", "jdoe"),
        },
        new Object[] {
          "(:caseIgnoreMatch:=foo)",
          new ExtensibleFilter("caseIgnoreMatch", null, "foo"),
        },
        new Object[] {
          "(uid:dn:caseIgnoreMatch:=jdoe)",
          new ExtensibleFilter("caseIgnoreMatch", "uid", "jdoe", true),
        },
        new Object[] {
          "(cn:caseExactMatch:=Fred Flintstone)",
          new ExtensibleFilter("caseExactMatch", "cn", "Fred Flintstone"),
        },
        new Object[] {
          "(cn:=Betty Rubble)",
          new ExtensibleFilter(null, "cn", "Betty Rubble"),
        },
        new Object[] {
          "(sn:dn:2.4.6.8.10:=Barney Rubble)",
          new ExtensibleFilter("2.4.6.8.10", "sn", "Barney Rubble", true),
        },
        new Object[] {
          "(o:dn:=Ace Industry)",
          new ExtensibleFilter(null, "o", "Ace Industry", true),
        },
        new Object[] {
          "(:1.2.3:=Wilma Flintstone)",
          new ExtensibleFilter("1.2.3", null, "Wilma Flintstone"),
        },
        new Object[] {
          "(:DN:2.4.6.8.10:=Dino)",
          new ExtensibleFilter("2.4.6.8.10", null, "Dino", true),
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
  public void parse(final String value, final ExtensibleFilter filter)
    throws Exception
  {
    Assert.assertEquals(ExtensibleFilter.parse(value), filter);
  }


  /**
   * Extensible test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new ExtensibleFilter(null, "uid", "jdoe"),
          new byte[] {
            (byte) 0xa9, 0x0b,
            (byte) 0x82, 0x03, 0x75, 0x69, 0x64,
            (byte) 0x83, 0x04, 0x6a, 0x64, 0x6f, 0x65, },
        },
        new Object[] {
          new ExtensibleFilter("caseIgnoreMatch", null, "foo"),
          new byte[] {
            (byte) 0xa9, 0x16,
            (byte) 0x81, 0x0f, 0x63, 0x61, 0x73, 0x65, 0x49, 0x67, 0x6e, 0x6f, 0x72, 0x65, 0x4d, 0x61, 0x74, 0x63, 0x68,
            (byte) 0x83, 0x03, 0x66, 0x6f, 0x6f, },
        },
        new Object[] {
          new ExtensibleFilter("caseIgnoreMatch", "uid", "jdoe", true),
          new byte[] {
            (byte) 0xa9, 0x1f,
            (byte) 0x81, 0x0f, 0x63, 0x61, 0x73, 0x65, 0x49, 0x67, 0x6e, 0x6f, 0x72, 0x65, 0x4d, 0x61, 0x74, 0x63, 0x68,
            (byte) 0x82, 0x03, 0x75, 0x69, 0x64,
            (byte) 0x83, 0x04, 0x6a, 0x64, 0x6f, 0x65,
            (byte) 0x84, 0x01, (byte) 0xff, },
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
  public void encode(final ExtensibleFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
