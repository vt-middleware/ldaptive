/*
  $Id: SortResponseControlTest.java 3005 2014-07-02 14:20:47Z dfisher $

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
import org.ldaptive.ResultCode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SortResponseControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SortResponseControlTest
{


  /**
   * Sort response control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "response")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // result code success
        // BER: 30:03:0A:01:00
        new Object[] {
          LdapUtils.base64Decode("MAMKAQA="),
          new SortResponseControl(ResultCode.SUCCESS, true),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  sort response control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "response"
  )
  public void decode(final byte[] berValue, final SortResponseControl expected)
    throws Exception
  {
    final SortResponseControl actual = new SortResponseControl(
      expected.getCriticality());
    actual.decode(berValue);
    Assert.assertEquals(actual, expected);
  }
}
