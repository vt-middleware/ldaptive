/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
  @Test(dataProvider = "component")
  public void encode(final ExtensibleFilter filter, final byte[] berValue)
    throws Exception
  {
    assertThat(filter.getEncoder().encode()).isEqualTo(berValue);
  }
}
