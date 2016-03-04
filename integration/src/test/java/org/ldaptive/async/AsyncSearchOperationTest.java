/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.async;

import java.util.concurrent.ExecutionException;
import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link AsyncSearchOperation}.
 *
 * @author  Middleware Services
 */
public class AsyncSearchOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry17")
  @BeforeClass(groups = {"async"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"async"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchAsyncDn",
      "searchAsyncFilter",
      "searchAsyncReturnAttrs",
      "searchAsyncResults"
    })
  @Test(groups = {"async"})
  public void search(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {

    final String expected = TestUtils.readFileIntoString(ldifFile);

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final AsyncSearchOperation search = new AsyncSearchOperation(conn);
      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      final FutureResponse<SearchResult> response = search.execute(request);
      AssertJUnit.assertTrue(response.getResult().size() > 0);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), response.getResult());
    } catch (ExecutionException | IllegalStateException e) {
      throw (Exception) e.getCause();
    }
  }
}
