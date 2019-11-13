/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
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
        // BER:
        // 64:6E:3A:75:69:64:3D:31:2C:6F:75:3D:74:65:73:74:2C:64:63:3D:6C:64:61:
        // 70:74:69:76:65:2C:64:63:3D:6F:72:67:
        new Object[] {
          LdapUtils.base64Decode("ZG46dWlkPTEsb3U9dGVzdCxkYz1sZGFwdGl2ZSxkYz1vcmc="),
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
  public void decode(final byte[] berValue, final AuthorizationIdentityResponseControl expected)
    throws Exception
  {
    final AuthorizationIdentityResponseControl actual = new AuthorizationIdentityResponseControl(
      expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
