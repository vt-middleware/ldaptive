/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.dn.Dn;
import org.testng.Assert;
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
  @BeforeClass(groups = "modifyDn")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "modifyDn")
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
  @Test(groups = "modifyDn")
  public void modifyDnLdapEntry(final String oldDn, final String newDn)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    Assert.assertTrue(search.execute(SearchRequest.objectScopeSearchRequest(oldDn)).entrySize() > 0);

    final ModifyDnOperation modifyDn = new ModifyDnOperation(TestUtils.createConnectionFactory());
    ModifyDnResponse response = modifyDn.execute(new ModifyDnRequest(oldDn, new Dn(newDn).getRDn().format(), true));
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    modifyDnLdapEntry = search.execute(SearchRequest.objectScopeSearchRequest(newDn)).getEntry();
    Assert.assertNotNull(modifyDnLdapEntry);
    try {
      final SearchResponse r = search.execute(SearchRequest.objectScopeSearchRequest(oldDn));
      Assert.assertEquals(r.getResultCode(), ResultCode.NO_SUCH_OBJECT);
    } catch (Exception e) {
      Assert.fail("Should have thrown LdapException, threw " + e);
    }
    response = modifyDn.execute(new ModifyDnRequest(newDn, new Dn(oldDn).getRDn().format(), true));
    Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
    Assert.assertTrue(search.execute(SearchRequest.objectScopeSearchRequest(oldDn)).entrySize() > 0);
    try {
      final SearchResponse r = search.execute(SearchRequest.objectScopeSearchRequest(newDn));
      Assert.assertEquals(r.getResultCode(), ResultCode.NO_SUCH_OBJECT);
    } catch (Exception e) {
      Assert.fail("Should have thrown LdapException, threw " + e);
    }
  }
}
