/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link EqualityFilter}.
 *
 * @author  Middleware Services
 */
public class EqualityFilterTest
{


  /**
   * Equality test data.
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
          "uid=",
          null,
        },
        new Object[] {
          "=",
          null,
        },
        new Object[] {
          "uid=jdoe",
          null,
        },
        new Object[] {
          "(=jdoe)",
          null,
        },
        new Object[] {
          "(uid=jdoe",
          null,
        },
        new Object[] {
          "uid=jdoe)",
          null,
        },
        new Object[] {
          "(uid=)",
          new EqualityFilter("uid", ""),
        },
        new Object[] {
          "(uid=jdoe)",
          new EqualityFilter("uid", "jdoe"),
        },
        new Object[] {
          "(o=Parens R Us \\28for all your parenthetical needs\\29)",
          new EqualityFilter("o", "Parens R Us (for all your parenthetical needs)"),
        },
        new Object[] {
          "(filename=C:\\5cMyFile)",
          new EqualityFilter("filename", "C:\\MyFile"),
        },
        new Object[] {
          "(bin=\\00\\00\\00\\04)",
          new EqualityFilter("bin", new String(new byte[] {0x00, 0x00, 0x00, 0x04})),
        },
        new Object[] {
          "(sn=Lu\\c4\\8di\\c4\\87)",
          new EqualityFilter("sn", "\u004C\u0075\u010D\u0069\u0107"),
        },
        new Object[] {
          "(1.3.6.1.4.1.1466.0=\\04\\02\\48\\69)",
          new EqualityFilter("1.3.6.1.4.1.1466.0", new String(new byte[] {0x04, 0x02, 0x48, 0x69})),
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
  public void parseRegex(final String value, final EqualityFilter filter)
    throws Exception
  {
    Assert.assertEquals(RegexFilterFunction.parseEqualityFilter(value), filter);
  }


  /**
   * Equality test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new EqualityFilter("uid", "jdoe"),
          new byte[] {
            (byte) 0xa3, 0x0b, 0x04, 0x03, 0x75, 0x69, 0x64, 0x04, 0x04, 0x6a, 0x64, 0x6f, 0x65, },
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
  public void encode(final EqualityFilter filter, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(filter.getEncoder().encode(), berValue);
  }
}
