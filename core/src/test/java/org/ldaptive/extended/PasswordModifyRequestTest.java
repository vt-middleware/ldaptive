/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PasswordModifyRequest}.
 *
 * @author  Middleware Services
 */
public class PasswordModifyRequestTest
{


  /**
   * Password modify test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new PasswordModifyRequest("uid=jdoe,ou=People,dc=example,dc=com", "secret123", null),
          new byte[] {
            // preamble
            0x30, 0x53, 0x02, 0x01, 0x01, 0x77, 0x4e,
            // oid
            (byte) 0x80, 0x17, 0x31, 0x2e, 0x33, 0x2e, 0x36, 0x2e, 0x31, 0x2e, 0x34, 0x2e, 0x31, 0x2e, 0x34, 0x32, 0x30,
            0x33, 0x2e, 0x31, 0x2e, 0x31, 0x31, 0x2e, 0x31,
            // extended request value
            (byte) 0x81, 0x33, 0x30, 0x31,
            // userIdentity
            (byte) 0x80, 0x24, 0x75, 0x69, 0x64, 0x3d, 0x6a, 0x64, 0x6f, 0x65, 0x2c, 0x6f, 0x75, 0x3d, 0x50, 0x65, 0x6f,
            0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d,
            0x63, 0x6f, 0x6d,
            // oldPasswd
            (byte) 0x81, 0x09, 0x73, 0x65, 0x63, 0x72, 0x65, 0x74, 0x31, 0x32, 0x33},
        },
      };
  }

  /**
   * @param  request  password modify request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final PasswordModifyRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(1), berValue);
  }
}
