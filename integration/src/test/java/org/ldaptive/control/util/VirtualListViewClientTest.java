/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.Iterator;
import org.ldaptive.AbstractTest;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.control.SortKey;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link VirtualListViewClient}.
 *
 * @author  Middleware Services
 */
public class VirtualListViewClientTest extends AbstractTest
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
      "createEntry19",
      "createEntry20",
      "createEntry21"
    })
  @BeforeClass(groups = "control-util")
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
  @AfterClass(groups = "control-util")
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
      "vlvSearchDn",
      "vlvSearchFilter"
    })
  @Test(groups = "control-util")
  public void execute(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    VirtualListViewClient client = new VirtualListViewClient(
      TestUtils.createConnectionFactory(),
      new SortKey("uid", "caseExactMatch"),
      new SortKey("givenName", "caseIgnoreMatch"));

    SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
    //request.setSortBehavior(SortBehavior.ORDERED);
    SearchResponse response = client.execute(request, new VirtualListViewParams(1, 0, 1));
    Iterator<LdapEntry> i = response.getEntries().iterator();
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertEquals(testLdapEntries[0].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[1].getDn(), i.next().getDn());

    response = client.execute(request, new VirtualListViewParams(2, 1, 1), response);
    i = response.getEntries().iterator();
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertEquals(testLdapEntries[0].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[1].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[2].getDn(), i.next().getDn());

    // VLV does not give a clean way to destroy the server side sorted entries
    // list. Open a new connection.
    client = new VirtualListViewClient(TestUtils.createConnectionFactory(), new SortKey("uid", "caseExactMatch"));

    request = new SearchRequest(dn, new SearchFilter(filter));
    //request.setSortBehavior(SortBehavior.ORDERED);
    response = client.execute(request, new VirtualListViewParams("21", 1, 0));
    i = response.getEntries().iterator();
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertEquals(testLdapEntries[1].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[2].getDn(), i.next().getDn());

    response = client.execute(request, new VirtualListViewParams("19", 0, 2), response);
    i = response.getEntries().iterator();
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertEquals(testLdapEntries[0].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[1].getDn(), i.next().getDn());
    AssertJUnit.assertEquals(testLdapEntries[2].getDn(), i.next().getDn());
  }
}
