/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import org.ldaptive.control.TreeDeleteControl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DeleteRequest}.
 *
 * @author  Middleware Services
 */
public class DeleteRequestTest
{


  /**
   * Delete test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new DeleteRequest.Builder().dn("uid=jdoe,ou=People,dc=example,dc=com").build(),
          new byte[] {
            // preamble
            0x30, 0x29, 0x02, 0x01, 0x02,
            // entry DN with application tag
            0x4a, 0x24, 0x75, 0x69, 0x64, 0x3d, 0x6a, 0x64, 0x6f, 0x65, 0x2c, 0x6f, 0x75, 0x3d, 0x50, 0x65, 0x6f, 0x70,
            0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63,
            0x6f, 0x6d},
        },
        new Object[] {
          new DeleteRequest.Builder().dn("dc=example,dc=com").controls(new TreeDeleteControl(true)).build(),
          new byte[] {
            // preamble
            0x30, 0x35, 0x02, 0x01, 0x02,
            // entry DN with application tag
            0x4a, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f,
            0x6d,
            // controls
            (byte) 0xa0, 0x1d, 0x30, 0x1b,
            0x04, 0x16, 0x31, 0x2e, 0x32, 0x2e, 0x38, 0x34, 0x30, 0x2e, 0x31, 0x31, 0x33, 0x35, 0x35, 0x36, 0x2e, 0x31,
            0x2e, 0x34, 0x2e, 0x38, 0x30, 0x35,
            0x01, 0x01, (byte) 0xff},
        },
      };
  }


  /**
   * @param  request  delete request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"provider"}, dataProvider = "request")
  public void encode(final DeleteRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(2), berValue);
  }
}
