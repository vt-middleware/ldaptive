/*
  $Id: SearchResultTest.java 2468 2012-08-07 18:54:52Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2468 $
  Updated: $Date: 2012-08-07 14:54:52 -0400 (Tue, 07 Aug 2012) $
*/
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchResult}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2468 $
 */
public class SearchResultTest extends AbstractTest
{

  /** Entry created for tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry7")
  @BeforeClass(groups = {"bean"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"bean"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to search for.
   * @param  filter  to search with.
   * @param  returnAttrs  attributes to return from search
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "toSearchResultsDn",
      "toSearchResultsFilter",
      "toSearchResultsAttrs",
      "toSearchResultsResults"
    }
  )
  @Test(groups = {"bean"})
  public void toSearchResults(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final SearchOperation search = new SearchOperation(conn);

      final SearchResult result = search.execute(
        new SearchRequest(
          dn, new SearchFilter(filter), returnAttrs.split("\\|"))).getResult();
      final String expected = TestUtils.readFileIntoString(ldifFile);
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } finally {
      conn.close();
    }
  }
}
