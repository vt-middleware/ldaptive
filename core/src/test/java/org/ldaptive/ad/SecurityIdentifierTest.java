/*
  $Id: SecurityIdentifierTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.ad;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SecurityIdentifier}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
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
  @Test(
    groups = {"ad"},
    dataProvider = "sids"
  )
  public void testToString(final String sidString, final byte[] sid)
    throws Exception
  {
    Assert.assertEquals(sidString, SecurityIdentifier.toString(sid));
  }


  /**
   * @param  sidString  objectSid string form
   * @param  sid  security identifier
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"ad"},
    dataProvider = "sids"
  )
  public void testToBytes(final String sidString, final byte[] sid)
    throws Exception
  {
    Assert.assertEquals(sid, SecurityIdentifier.toBytes(sidString));
  }
}
