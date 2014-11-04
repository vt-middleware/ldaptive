/*
  $Id: SearchValidatorTest.java 2668 2013-03-14 20:38:18Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2668 $
  Updated: $Date: 2013-03-14 16:38:18 -0400 (Thu, 14 Mar 2013) $
*/
package org.ldaptive.pool;

import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link SearchValidator}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2668 $
 */
public class SearchValidatorTest extends AbstractTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"validator"})
  public void defaultSettings()
    throws Exception
  {
    final Connection c = TestUtils.createConnection();
    final SearchValidator sv = new SearchValidator();
    try {
      c.open();
      AssertJUnit.assertTrue(sv.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(sv.validate(c));
  }


  /**
   * @param  searchDn  to test with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"validator"})
  @Parameters("ldapBaseDn")
  public void customSettings(final String searchDn)
    throws Exception
  {
    final Connection c = TestUtils.createConnection();
    final SearchValidator sv = new SearchValidator(
      new SearchRequest(searchDn, new SearchFilter("(cn=*)")));
    try {
      c.open();
      AssertJUnit.assertTrue(sv.validate(c));
      sv.getSearchRequest().setSearchFilter(new SearchFilter("(dne=*)"));
      AssertJUnit.assertFalse(sv.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(sv.validate(c));
  }
}
