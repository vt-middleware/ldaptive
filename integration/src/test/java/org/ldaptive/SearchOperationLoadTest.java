/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.HashMap;
import java.util.Map;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Load test for {@link SearchOperation}.
 *
 * @author  Middleware Services
 */
public class SearchOperationLoadTest extends AbstractTest
{

  /** Entries for auth tests. */
  private static final Map<String, LdapEntry[]> ENTRIES = new HashMap<>();

  static {
    // Initialize the map of entries
    for (int i = 2; i <= 10; i++) {
      ENTRIES.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Base DN to search on. */
  private String searchBaseDn;

  /** Search operation instance for concurrency testing. */
  private SearchOperation singleTLSSearch;

  /** Search operation instance for concurrency testing. */
  private SearchOperation pooledTLSSearch;


  /**
   * @param  baseDn  to search on.
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   * @param  ldifFile6  to create.
   * @param  ldifFile7  to create.
   * @param  ldifFile8  to create.
   * @param  ldifFile9  to create.
   * @param  ldifFile10  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "ldapBaseDn",
    "createEntry2",
    "createEntry3",
    "createEntry4",
    "createEntry5",
    "createEntry6",
    "createEntry7",
    "createEntry8",
    "createEntry9",
    "createEntry10"
  })
  @BeforeClass(groups = "searchload")
  // CheckStyle:ParameterNumber OFF
  public void createSearchEntry(
    final String baseDn,
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5,
    final String ldifFile6,
    final String ldifFile7,
    final String ldifFile8,
    final String ldifFile9,
    final String ldifFile10)
    throws Exception
  {
    singleTLSSearch = new SearchOperation(TestUtils.createConnectionFactory());
    singleTLSSearch.setEntryHandlers(
      CaseChangeEntryHandler.builder().dnCaseChange(CaseChangeEntryHandler.CaseChange.LOWER).build());
    singleTLSSearch.setSearchResultHandlers(
      new FollowSearchReferralHandler(),
      new FollowSearchResultReferenceHandler());
    pooledTLSSearch = new SearchOperation(TestUtils.createPooledConnectionFactory());
    pooledTLSSearch.setEntryHandlers(
      CaseChangeEntryHandler.builder().dnCaseChange(CaseChangeEntryHandler.CaseChange.LOWER).build());
    pooledTLSSearch.setSearchResultHandlers(
      new FollowSearchReferralHandler(),
      new FollowSearchResultReferenceHandler());

    searchBaseDn = baseDn;
    // CheckStyle:Indentation OFF
    ENTRIES.get("2")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    ENTRIES.get("3")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    ENTRIES.get("4")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile4)).getEntry();
    ENTRIES.get("5")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile5)).getEntry();
    ENTRIES.get("6")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile6)).getEntry();
    ENTRIES.get("7")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile7)).getEntry();
    ENTRIES.get("8")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile8)).getEntry();
    ENTRIES.get("9")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile9)).getEntry();
    ENTRIES.get("10")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile10)).getEntry();
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }
  }
  // CheckStyle:ParameterNumber ON


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "searchload")
  public void deleteSearchEntry()
    throws Exception
  {
    super.deleteLdapEntry(ENTRIES.get("2")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("3")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("4")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("5")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("6")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("7")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("8")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("9")[0].getDn());
    super.deleteLdapEntry(ENTRIES.get("10")[0].getDn());

    pooledTLSSearch.getConnectionFactory().close();
  }


  /**
   * Sample authentication data.
   *
   * @return  user authentication data
   */
  @DataProvider(name = "search-data")
  public Object[][] createAuthData()
  {
    return
      new Object[][] {
        {
          "(mail=jadams@ldaptive.org)",
          "cn",
          "cn=John Adams",
        },
        {
          "(mail=jadams-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=tjefferson@ldaptive.org)",
          "givenName|sn",
          "givenName=Thomas|sn=Jefferson",
        },
        {
          "(mail=tjefferson-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=jmadison@ldaptive.org)",
          "givenName|sn",
          "givenName=James|sn=Madison",
        },
        {
          "(mail=jmadison-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=jmonroe@ldaptive.org)",
          "givenName|sn",
          "givenName=James|sn=Monroe",
        },
        {
          "(mail=jmonroe-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=jqadams@ldaptive.org)",
          "cn",
          "cn=John Quincy Adams",
        },
        {
          "(mail=jqadams-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=ajackson@ldaptive.org)",
          "givenName|sn",
          "givenName=Andrew|sn=Jackson",
        },
        {
          "(mail=ajackson-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=mvburen@ldaptive.org)",
          "givenName|sn",
          "givenName=Martin|sn=Buren",
        },
        {
          "(mail=mvburen-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=whharrison@ldaptive.org)",
          "givenName|sn",
          "givenName=William|sn=Harrison",
        },
        {
          "(mail=whharrison-wrong@ldaptive.org)",
          null,
          null,
        },
        {
          "(mail=jtyler@ldaptive.org)",
          "givenName|sn",
          "givenName=John|sn=Tyler",
        },
        {
          "(mail=jtyler-wrong@ldaptive.org)",
          null,
          null,
        },
      };
  }


  /**
   * @param  filter  to execute.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = "searchload", dataProvider = "search-data", threadPoolSize = 3, invocationCount = 100, timeOut = 60000)
  public void search(
    final String filter,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (returnAttrs == null) {
      final SearchResponse response = singleTLSSearch.execute(new SearchRequest(searchBaseDn, filter));
      Assert.assertTrue(response.isSuccess());
      Assert.assertNull(response.getEntry());
      return;
    }
    // test search with return attributes
    final LdapEntry expected = TestUtils.convertStringToEntry(null, expectedAttrs);
    final SearchResponse response = singleTLSSearch.execute(
      new SearchRequest(searchBaseDn, filter, returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    expected.setDn(response.getEntry().getDn());
    TestUtils.assertEquals(expected, response.getEntry());
  }


  /**
   * @param  filter  to execute.
   * @param  returnAttrs  to search for.
   * @param  expectedAttrs  to expect from the search.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = "searchload", dataProvider = "search-data", threadPoolSize = 3, invocationCount = 100, timeOut = 60000)
  public void searchPooled(
    final String filter,
    final String returnAttrs,
    final String expectedAttrs)
    throws Exception
  {
    if (returnAttrs == null) {
      final SearchResponse response = pooledTLSSearch.execute(new SearchRequest(searchBaseDn, filter));
      Assert.assertTrue(response.isSuccess());
      Assert.assertNull(response.getEntry());
      return;
    }
    // test search with return attributes
    final LdapEntry expected = TestUtils.convertStringToEntry(null, expectedAttrs);
    final SearchResponse response = pooledTLSSearch.execute(
      new SearchRequest(searchBaseDn, filter, returnAttrs.split("\\|")));
    Assert.assertTrue(response.isSuccess());
    expected.setDn(response.getEntry().getDn());
    TestUtils.assertEquals(expected, response.getEntry());
  }
}
