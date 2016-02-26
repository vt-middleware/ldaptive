/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ModifyDnOperation}.
 *
 * @author  Middleware Services
 */
public class ModifyDnOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;

  /** Entry created for ldap tests. */
  private static LdapEntry modifyDnLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry5")
  @BeforeClass(groups = {"modifyDn"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"modifyDn"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    if (modifyDnLdapEntry != null) {
      super.deleteLdapEntry(modifyDnLdapEntry.getDn());
    }
  }


  /**
   * @param  oldDn  to rename.
   * @param  newDn  to rename to.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "modifyOldDn", "modifyNewDn" })
  @Test(groups = {"modifyDn"})
  public void modifyDnLdapEntry(final String oldDn, final String newDn)
    throws Exception
  {
    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      AssertJUnit.assertTrue(search.execute(SearchRequest.newObjectScopeSearchRequest(oldDn)).getResult().size() > 0);

      final ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
      Response<Void> response = modifyDn.execute(new ModifyDnRequest(oldDn, newDn));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      modifyDnLdapEntry = search.execute(SearchRequest.newObjectScopeSearchRequest(newDn)).getResult().getEntry();
      AssertJUnit.assertNotNull(modifyDnLdapEntry);
      try {
        final Response<SearchResult> r = search.execute(SearchRequest.newObjectScopeSearchRequest(oldDn));
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, r.getResultCode());
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      } catch (Exception e) {
        AssertJUnit.fail("Should have thrown LdapException, threw " + e);
      }
      response = modifyDn.execute(new ModifyDnRequest(newDn, oldDn));
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertTrue(search.execute(SearchRequest.newObjectScopeSearchRequest(oldDn)).getResult().size() > 0);
      try {
        final Response<SearchResult> r = search.execute(SearchRequest.newObjectScopeSearchRequest(newDn));
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, r.getResultCode());
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      } catch (Exception e) {
        AssertJUnit.fail("Should have thrown LdapException, threw " + e);
      }
    }
  }
}
