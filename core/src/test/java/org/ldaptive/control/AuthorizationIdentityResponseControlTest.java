/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DefaultDERBuffer;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AuthorizationIdentityResponseControl}.
 *
 * @author  Middleware Services
 */
public class AuthorizationIdentityResponseControlTest
{


  /**
   * Authorization identity response control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // dn:uid=1,ou=test,dc=ldaptive,dc=org
        new Object[] {
          new DefaultDERBuffer(
            new byte[] {
              0x64, 0x6E, 0x3A, 0x75, 0x69, 0x64, 0x3D, 0x31, 0x2C, 0x6F, 0x75, 0x3D, 0x74, 0x65, 0x73, 0x74, 0x2C,
              0x64, 0x63, 0x3D, 0x6C, 0x64, 0x61, 0x70, 0x74, 0x69, 0x76, 0x65, 0x2C, 0x64, 0x63, 0x3D, 0x6F, 0x72,
              0x67}),
          new AuthorizationIdentityResponseControl("dn:uid=1,ou=test,dc=ldaptive,dc=org"),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  authorization identity response control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "response")
  public void decode(final DERBuffer berValue, final AuthorizationIdentityResponseControl expected)
    throws Exception
  {
    final AuthorizationIdentityResponseControl actual = new AuthorizationIdentityResponseControl(
      expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
