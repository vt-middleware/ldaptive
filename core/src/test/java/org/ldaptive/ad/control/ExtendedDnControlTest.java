/*
  $Id: ExtendedDnControlTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive.ad.control;

import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ExtendedDnControl}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class ExtendedDnControlTest
{


  /**
   * Extended DN control test data.
   *
   * @return  response test data
   */
  @DataProvider(name = "request")
  public Object[][] createData()
  {
    return
      new Object[][] {
        // standard format
        // BER: 30:03:02:01:01:
        new Object[] {
          LdapUtils.base64Decode("MAMCAQE="),
          new ExtendedDnControl(),
        },
        // hexadecimal format
        // BER: 30:03:02:01:00
        new Object[] {
          LdapUtils.base64Decode("MAMCAQA="),
          new ExtendedDnControl(ExtendedDnControl.Flag.HEXADECIMAL),
        },
      };
  }


  /**
   * @param  berValue  to encode.
   * @param  expected  extended dn control to test.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"control"},
    dataProvider = "request"
  )
  public void encode(final byte[] berValue, final ExtendedDnControl expected)
    throws Exception
  {
    Assert.assertEquals(expected.encode(), berValue);
  }
}
