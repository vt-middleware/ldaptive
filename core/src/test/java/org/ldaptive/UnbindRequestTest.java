/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link UnbindRequest}.
 *
 * @author  Middleware Services
 */
public class UnbindRequestTest
{


  /**
   * Unbind test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new UnbindRequest(),
          new byte[] {
            // preamble
            0x30, 0x05, 0x02, 0x01, 0x05,
            // unbind op
            0x42, 0x00},
        },
      };
  }


  /**
   * @param  request  unbind request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final UnbindRequest request, final byte[] berValue)
    throws Exception
  {
    assertThat(request.encode(5)).isEqualTo(berValue);
  }
}
