/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ProxyAuthorizationControl}.
 *
 * @author  Middleware Services
 */
public class ProxyAuthorizationControlTest
{


  /**
   * Proxy authorization control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        new Object[] {
          new byte[0],
          new ProxyAuthorizationControl(),
        },
        // BER:
        // 64:6E:3A:75:69:64:3D:31:2C:6F:75:3D:74:65:73:74:2C:64:63:3D:6C:64:
        // 61:70:74:69:76:65:2C:64:63:3D:6F:72:67
        new Object[] {
          LdapUtils.base64Decode("ZG46dWlkPTEsb3U9dGVzdCxkYz1sZGFwdGl2ZSxkYz1vcmc="),
          new ProxyAuthorizationControl("dn:uid=1,ou=test,dc=ldaptive,dc=org"),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  proxy authorization control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "control", dataProvider = "request")
  public void decode(final byte[] berValue, final ProxyAuthorizationControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
