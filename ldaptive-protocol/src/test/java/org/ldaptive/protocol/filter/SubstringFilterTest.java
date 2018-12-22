/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SubstringFilter}.
 *
 * @author  Middleware Services
 */
public class SubstringFilterTest
{


  /**
   * Substring test data.
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
          "cn=",
          null,
        },
        new Object[] {
          "=",
          null,
        },
        new Object[] {
          "cn=John*",
          null,
        },
        new Object[] {
          "(cn=)",
          null,
        },
        new Object[] {
          "(cn=*)",
          null,
        },
        new Object[] {
          "(=John*)",
          null,
        },
        new Object[] {
          "(cn=John*",
          null,
        },
        new Object[] {
          "cn=John*)",
          null,
        },
        new Object[] {
          "(cn=John*)",
          new SubstringFilter("cn", "John", null, null),
        },
        new Object[] {
          "(cn=John**)",
          null,
        },
        new Object[] {
          "(cn=*John*)",
          new SubstringFilter("cn", null, null, "John"),
        },
        new Object[] {
          "(cn=*John*Doe*)",
          new SubstringFilter("cn", null, null, "John", "Doe"),
        },
        new Object[] {
          "(cn=*Doe)",
          new SubstringFilter("cn", null, "Doe", null),
        },
        new Object[] {
          "(cn=J*i*h*n*D*o*e)",
          new SubstringFilter("cn", "J", "e", "i", "h", "n", "D", "o"),
        },
        new Object[] {
          "(cn=John*Doe)",
          new SubstringFilter("cn", "John", "Doe", null),
        },
        new Object[] {
          "(cn=*\\2a*)",
          new SubstringFilter("cn", null, null, "*"),
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
  public void parse(final String value, final SubstringFilter filter)
    throws Exception
  {
    Assert.assertEquals(SubstringFilter.parse(value), filter);
  }


  /**
   * Substring test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new SubstringFilter("cn", "abc", null, null),
          new byte[] {
            (byte) 0xa4, 0x0b,
            0x04, 0x02, 0x63, 0x6e,
            0x30, 0x05,
            (byte) 0x80, 0x03, 0x61, 0x62, 0x63, },
        },
        new Object[] {
          new SubstringFilter("cn", null, null, "lmn"),
          new byte[] {
            (byte) 0xa4, 0x0b,
            0x04, 0x02, 0x63, 0x6e,
            0x30, 0x05,
            (byte) 0x81, 0x03, 0x6c, 0x6d, 0x6e, },
        },
        new Object[] {
          new SubstringFilter("cn", null, "xyz", null),
          new byte[] {
            (byte) 0xa4, 0x0b,
            0x04, 0x02, 0x63, 0x6e,
            0x30, 0x05,
            (byte) 0x82, 0x03, 0x78, 0x79, 0x7a, },
        },
        new Object[] {
          new SubstringFilter("cn", "abc", "xyz", "def", "lmn", "uvw"),
          new byte[] {
            (byte) 0xa4, 0x1f,
            0x04, 0x02, 0x63, 0x6e,
            0x30, 0x19,
            (byte) 0x80, 0x03, 0x61, 0x62, 0x63,
            (byte) 0x81, 0x03, 0x64, 0x65, 0x66,
            (byte) 0x81, 0x03, 0x6c, 0x6d, 0x6e,
            (byte) 0x81, 0x03, 0x75, 0x76, 0x77,
            (byte) 0x82, 0x03, 0x78, 0x79, 0x7a, },
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
  public void encode(final SubstringFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
