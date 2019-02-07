/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AnonymousBindRequest}.
 *
 * @author  Middleware Services
 */
public class AnonymousBindRequestTest
{


  /**
   * Anonymous bind test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new AnonymousBindRequest(),
          new byte[] {
            // preamble
            0x30, 0x0c, 0x02, 0x01, 0x01, 0x60, 0x07, 0x02, 0x01, 0x03,
            // empty bind dn
            0x04, 0x00,
            // empty password
            (byte) 0x80, 0x00},
        },
      };
  }

  /**
   * @param  request  anonymous bind request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "provider", dataProvider = "request")
  public void encode(final AnonymousBindRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(1), berValue);
  }
}
