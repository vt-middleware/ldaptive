/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.filter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link OrFilter}.
 *
 * @author  Middleware Services
 */
public class OrFilterTest
{


  /**
   * Or test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "component")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new OrFilter(new EqualityFilter("givenName", "John"), new EqualityFilter("givenName", "Jonathan")),
          new byte[] {
            (byte) 0xa1, 0x2a,
            (byte) 0xa3, 0x11,
            0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6e, 0x4e, 0x61, 0x6d, 0x65,
            0x04, 0x04, 0x4a, 0x6f, 0x68, 0x6e,
            (byte) 0xa3, 0x15,
            0x04, 0x09, 0x67, 0x69, 0x76, 0x65, 0x6e, 0x4e, 0x61, 0x6d, 0x65,
            0x04, 0x08, 0x4a, 0x6f, 0x6e, 0x61, 0x74, 0x68, 0x61, 0x6e, },
        },
        new Object[] {
          new OrFilter(),
          new byte[] {
            (byte) 0xa1, 0x00, },
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
  public void encode(final OrFilter filter, final byte[] berValue)
    throws Exception
  {
    assertThat(filter.getEncoder().encode()).isEqualTo(berValue);
  }
}
