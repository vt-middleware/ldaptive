/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AddRequest}.
 *
 * @author  Middleware Services
 */
public class AddRequestTest
{


  /**
   * Add test data.
   *
   * @return  request test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          AddRequest.builder()
            .dn("dc=example,dc=com")
            .attributes(new LdapAttribute("objectClass", "top", "domain"), new LdapAttribute("dc", "example")).build(),
          new byte[] {
            // preamble
            0x30, 0x49, 0x02, 0x01, 0x02,
            // modify op
            0x68, 0x44,
            // entry DN
            0x04, 0x11, 0x64, 0x63, 0x3d, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65, 0x2c, 0x64, 0x63, 0x3d, 0x63, 0x6f,
            0x6d,
            // attributes
            0x30, 0x2f,
            // first attribute
            0x30, 0x1c, 0x04, 0x0b, 0x6f, 0x62, 0x6a, 0x65, 0x63, 0x74, 0x43, 0x6c, 0x61, 0x73, 0x73,
            0x31, 0x0d, 0x04, 0x03, 0x74, 0x6f, 0x70, 0x04, 0x06, 0x64, 0x6f, 0x6d, 0x61, 0x69, 0x6e,
            // second attribute
            0x30, 0x0f, 0x04, 0x02, 0x64, 0x63,
            0x31, 0x09, 0x04, 0x07, 0x65, 0x78, 0x61, 0x6d, 0x70, 0x6c, 0x65},
        },
      };
  }


  /**
   * @param  request  add request to encode.
   * @param  berValue  expected value.
   *
   * @throws  Exception  On test failure.
   */
  @Test(dataProvider = "request")
  public void encode(final AddRequest request, final byte[] berValue)
    throws Exception
  {
    Assert.assertEquals(request.encode(2), berValue);
  }
}
