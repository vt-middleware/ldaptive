/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchResponse}.
 *
 * @author  Middleware Services
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
  @BeforeClass(groups = "bean")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "bean")
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
  @Parameters({
    "toSearchResultsDn",
    "toSearchResultsFilter",
    "toSearchResultsAttrs",
    "toSearchResultsResults"
  })
  @Test(groups = "bean")
  public void toSearchResults(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final SearchResponse result = search.execute(new SearchRequest(dn, filter, returnAttrs.split("\\|")));
    final String expected = TestUtils.readFileIntoString(ldifFile);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
  }
}
