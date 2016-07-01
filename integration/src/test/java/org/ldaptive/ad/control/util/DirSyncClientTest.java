/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control.util;

import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.ad.control.DirSyncControl;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DirSyncClient}.
 *
 * @author  Middleware Services
 */
public class DirSyncClientTest extends AbstractTest
{

  /** Entries created for ldap tests. */
  private static LdapEntry[] testLdapEntries;


  /**
   * @param  ldifFile1  to create.
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry27",
      "createEntry28",
      "createEntry29"
    })
  @BeforeClass(groups = {"control-util"})
  public void createLdapEntry(final String ldifFile1, final String ldifFile2, final String ldifFile3)
    throws Exception
  {
    testLdapEntries = new LdapEntry[3];
    testLdapEntries[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile1)).getEntry();
    super.createLdapEntry(testLdapEntries[0]);
    testLdapEntries[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    super.createLdapEntry(testLdapEntries[1]);
    testLdapEntries[2] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    super.createLdapEntry(testLdapEntries[2]);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"control-util"})
  public void deleteLdapEntry()
    throws Exception
  {
    for (LdapEntry testLdapEntry : testLdapEntries) {
      super.deleteLdapEntry(testLdapEntry.getDn());
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsSearchDn",
      "dsSearchFilter"
    })
  @Test(groups = {"control-util"})
  public void execute(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }
    if (TestControl.isOpenDJProvider()) {
      throw new UnsupportedOperationException("OpenDJ will not parse DNs used by this control");
    }

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final DirSyncClient client = new DirSyncClient(
        conn,
        new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });

      final SearchRequest request = new SearchRequest(
        dn.substring(dn.indexOf(",") + 1, dn.length()),
        new SearchFilter(filter),
        "uid");
      final Response<SearchResult> response = client.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertTrue(response.getResult().size() > 0);
      AssertJUnit.assertFalse(client.hasMore(response));
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsSearchDn",
      "dsSearchFilter"
    })
  @Test(groups = {"control-util"})
  public void executeToCompletion(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }
    if (TestControl.isOpenDJProvider()) {
      throw new UnsupportedOperationException("OpenDJ will not parse DNs used by this control");
    }

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final DirSyncClient client = new DirSyncClient(
        conn,
        new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });

      final SearchRequest request = new SearchRequest(
        dn.substring(dn.indexOf(",") + 1, dn.length()),
        new SearchFilter(filter),
        "uid");
      final Response<SearchResult> response = client.executeToCompletion(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertTrue(response.getResult().size() > 0);
    }
  }
}
