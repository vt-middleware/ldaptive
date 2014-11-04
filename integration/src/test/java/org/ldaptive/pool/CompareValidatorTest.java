/*
  $Id: CompareValidatorTest.java 2513 2012-09-28 21:18:33Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2513 $
  Updated: $Date: 2012-09-28 17:18:33 -0400 (Fri, 28 Sep 2012) $
*/
package org.ldaptive.pool;

import org.ldaptive.AbstractTest;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link CompareValidator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2513 $
 */
public class CompareValidatorTest extends AbstractTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"validator"})
  public void defaultSettings()
    throws Exception
  {
    final Connection c = TestUtils.createConnection();
    final CompareValidator sv = new CompareValidator();
    try {
      c.open();
      AssertJUnit.assertTrue(sv.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(sv.validate(c));
  }


  /**
   * @param  compareDn  to test with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"validator"})
  @Parameters("ldapBindDn")
  public void customSettings(final String compareDn)
    throws Exception
  {
    final Connection c = TestUtils.createConnection();
    final CompareValidator cv = new CompareValidator(
      new CompareRequest(
        compareDn,
        new LdapAttribute("objectClass", "inetOrgPerson")));
    try {
      c.open();
      AssertJUnit.assertTrue(cv.validate(c));
      cv.getCompareRequest().setDn("cn=does-not-exist");
      AssertJUnit.assertFalse(cv.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(cv.validate(c));
  }
}
