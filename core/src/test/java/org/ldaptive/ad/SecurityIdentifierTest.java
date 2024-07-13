/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad;

import org.ldaptive.LdapUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link SecurityIdentifier}.
 *
 * @author  Middleware Services
 */
public class SecurityIdentifierTest
{


  /**
   * ObjectSid test data.
   *
   * @return  ldap attribute values
   *
   * @throws  Exception  if test data cannot be generated
   */
  @DataProvider(name = "sids")
  public Object[][] createSids()
    throws Exception
  {
    return
      new Object[][] {
        new Object[] {
          "S-1-5-21-1051162837-3568060411-1686669321-1105",
          LdapUtils.base64Decode("AQUAAAAAAAUVAAAA1XinPvtHrNQJiIhkUQQAAA=="),
        },
        new Object[] {
          "S-1-5-21-1051162837-3568060411-1686669321-3173",
          LdapUtils.base64Decode("AQUAAAAAAAUVAAAA1XinPvtHrNQJiIhkZQwAAA=="),
        },
        new Object[] {
          "S-1-5-21-1051162837-3568060411-1686669321-1000",
          LdapUtils.base64Decode("AQUAAAAAAAUVAAAA1XinPvtHrNQJiIhk6AMAAA=="),
        },
      };
  }


  /**
   * @param  sidString  objectSid string form
   * @param  sid  security identifier
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ad", dataProvider = "sids")
  public void testToString(final String sidString, final byte[] sid)
    throws Exception
  {
    assertThat(SecurityIdentifier.toString(sid)).isEqualTo(sidString);
  }


  /**
   * @param  sidString  objectSid string form
   * @param  sid  security identifier
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "ad", dataProvider = "sids")
  public void testToBytes(final String sidString, final byte[] sid)
    throws Exception
  {
    assertThat(SecurityIdentifier.toBytes(sidString)).isEqualTo(sid);
  }
}
