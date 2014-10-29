/*
  $Id: PasswordExpiringControlTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PasswordExpiringControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class PasswordExpiringControlTest
{


  /**
   * Password expiring control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // BER: 34:36:31
        new Object[] {
          LdapUtils.base64Decode("NDYx"),
          new PasswordExpiringControl(461),
        },
        // BER: 33:38:34
        new Object[] {
          LdapUtils.base64Decode("Mzg0"),
          new PasswordExpiringControl(384),
        },
      };
  }


  /**
   * @param  berValue  to decode.
   * @param  expected  password expiring control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "response"
  )
  public void decode(
    final byte[] berValue,
    final PasswordExpiringControl expected)
    throws Exception
  {
    final PasswordExpiringControl actual = new PasswordExpiringControl(
      expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
