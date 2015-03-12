/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ldaptive.ad.control.ForceUpdateControl;
import org.ldaptive.ad.control.GetStatsControl;
import org.ldaptive.ad.control.LazyCommitControl;
import org.ldaptive.ad.control.PermissiveModifyControl;
import org.ldaptive.ad.control.RangeRetrievalNoerrControl;
import org.ldaptive.ad.control.SearchOptionsControl;
import org.ldaptive.ad.control.ShowDeactivatedLinkControl;
import org.ldaptive.ad.control.ShowDeletedControl;
import org.ldaptive.ad.control.ShowRecycledControl;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.ad.handler.ObjectSidHandler;
import org.ldaptive.ad.handler.PrimaryGroupIdHandler;
import org.ldaptive.ad.handler.RangeEntryHandler;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.ProxyAuthorizationControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.control.VirtualListViewRequestControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.CaseChangeEntryHandler.CaseChange;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.NoOpEntryHandler;
import org.ldaptive.handler.RecursiveEntryHandler;
import org.ldaptive.handler.SearchEntryHandler;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.ldaptive.pool.BlockingConnectionPool;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.referral.SearchReferralHandler;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchOperation}.
 *
 * @author  Middleware Services
 */
public class SearchOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;

  /** Entry created for ldap tests. */
  private static LdapEntry specialCharsLdapEntry;

  /** Entries for group tests. */
  private static final Map<String, LdapEntry[]> GROUP_ENTRIES = new HashMap<>();

  /**
   * Initialize the map of group entries.
   */
  static {
    for (int i = 2; i <= 5; i++) {
      GROUP_ENTRIES.put(String.valueOf(i), new LdapEntry[2]);
    }
  }

  /** Connection instance for concurrency testing. */
  protected Connection singleConn;


  /**
   * Default constructor.
   *
   * @throws  Exception  On test failure.
   */
  public SearchOperationTest()
    throws Exception
  {
    singleConn = TestUtils.createConnection();
  }


  /** @throws  Exception  On test failure. */
  @BeforeClass(groups = {"search"})
  public void openConnection()
    throws Exception
  {
    singleConn.open();
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry2")
  @BeforeClass(groups = {"search", "searchInit"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /**
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   * @param  ldifFile4  to create.
   * @param  ldifFile5  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createGroup2",
      "createGroup3",
      "createGroup4",
      "createGroup5"
    }
  )
  @BeforeClass(groups = {"search"}, dependsOnGroups = {"searchInit"})
  public void createGroupEntry(
    final String ldifFile2,
    final String ldifFile3,
    final String ldifFile4,
    final String ldifFile5)
    throws Exception
  {
    // CheckStyle:Indentation OFF
    GROUP_ENTRIES.get("2")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    GROUP_ENTRIES.get("3")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    GROUP_ENTRIES.get("4")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile4)).getEntry();
    GROUP_ENTRIES.get("5")[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile5)).getEntry();
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : GROUP_ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }

    final String baseDn = DnParser.substring(GROUP_ENTRIES.get("2")[0].getDn(), 1);
    // setup group relationships
    final Connection conn = TestUtils.createSetupConnection();
    try {
      conn.open();

      final ModifyOperation modify = new ModifyOperation(conn);
      try {
        modify.execute(
          new ModifyRequest(
            GROUP_ENTRIES.get("2")[0].getDn(),
            new AttributeModification(
              AttributeModificationType.ADD,
              new LdapAttribute("member", "cn=Group 3," + baseDn))));
      } catch (LdapException e) {
        // ignore attribute already exists
        if (ResultCode.ATTRIBUTE_OR_VALUE_EXISTS != e.getResultCode()) {
          throw e;
        }
      }
      try {
        modify.execute(
          new ModifyRequest(
            GROUP_ENTRIES.get("3")[0].getDn(),
            new AttributeModification(
              AttributeModificationType.ADD,
              new LdapAttribute("member", "cn=Group 4," + baseDn, "cn=Group 5," + baseDn))));
      } catch (LdapException e) {
        // ignore attribute already exists
        if (ResultCode.ATTRIBUTE_OR_VALUE_EXISTS != e.getResultCode()) {
          throw e;
        }
      }
      try {
        modify.execute(
          new ModifyRequest(
            GROUP_ENTRIES.get("4")[0].getDn(),
            new AttributeModification(
              AttributeModificationType.ADD,
              new LdapAttribute("member", "cn=Group 2," + baseDn, "cn=Group 3," + baseDn))));
      } catch (LdapException e) {
        // ignore attribute already exists
        if (ResultCode.ATTRIBUTE_OR_VALUE_EXISTS != e.getResultCode()) {
          throw e;
        }
      }
    } finally {
      conn.close();
    }
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry")
  @BeforeClass(groups = {"search"})
  public void createSpecialCharsEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    specialCharsLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"search"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry.getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("2")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("3")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("4")[0].getDn());
    super.deleteLdapEntry(GROUP_ENTRIES.get("5")[0].getDn());
  }


  /**
   * @param  createNew  whether to construct a new connection.
   *
   * @return  connection
   *
   * @throws  Exception  On connection failure.
   */
  public Connection createLdapConnection(final boolean createNew)
    throws Exception
  {
    if (createNew) {
      return TestUtils.createConnection();
    }
    return singleConn;
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterParameters",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(
    groups = {"search"}, threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void search(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final SearchResult entryDnResult = TestUtils.convertLdifToResult(expected);
    entryDnResult.getEntry().addAttribute(new LdapAttribute("entryDN", entryDnResult.getEntry().getDn()));

    // test searching
    SearchResult result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    // test searching no attributes
    result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), ReturnAttributes.NONE.value())).getResult();
    AssertJUnit.assertTrue(result.getEntry().getAttributes().isEmpty());

    // test searching without handler
    final SearchRequest sr = new SearchRequest(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"));
    sr.setSearchEntryHandlers(new SearchEntryHandler[0]);
    result = search.execute(sr).getResult();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    // test searching with multiple handlers
    final DnAttributeEntryHandler srh = new DnAttributeEntryHandler();
    sr.setSearchEntryHandlers(new NoOpEntryHandler(), srh);
    result = search.execute(sr).getResult();
    // ignore the case of entryDN; some directories return those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("entryDN")).compare(entryDnResult.getEntry(), result.getEntry()));

    // test that entry dn handler is no-op if attribute name conflicts
    srh.setDnAttributeName("givenName");
    sr.setSearchEntryHandlers(new NoOpEntryHandler(), srh);
    result = search.execute(sr).getResult();
    // ignore the case of entryDN; some directories return those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("entryDN")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        result.getEntry()));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterParameters",
      "searchResults"
    }
  )
  @Test(groups = {"search"})
  public void returnAttributesSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final SearchResult entryDnResult = TestUtils.convertLdifToResult(expected);

    // test searching, no attributes
    SearchResult result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), ReturnAttributes.NONE.value())).getResult();
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertTrue(result.getEntry().getAttributes().isEmpty());

    // test searching, user attributes
    result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), ReturnAttributes.ALL_USER.value())).getResult();
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("cn"));
    AssertJUnit.assertNull(result.getEntry().getAttribute("createTimestamp"));

    // test searching, operations attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")),
          ReturnAttributes.ALL_OPERATIONAL.add("createTimestamp"))).getResult();
    } else {
      result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")),
          ReturnAttributes.ALL_OPERATIONAL.value())).getResult();
    }
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertNull(result.getEntry().getAttribute("cn"));
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("createTimestamp"));

    // test searching, all attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")),
          ReturnAttributes.ALL.add("createTimestamp"))).getResult();
    } else {
      result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), ReturnAttributes.ALL.value())).getResult();
    }
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("cn"));
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("createTimestamp"));
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
      "pagedSearchDn",
      "pagedSearchFilter",
      "pagedSearchReturnAttrs",
      "pagedSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void pagedSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final PagedResultsControl prc = new PagedResultsControl(1, true);
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      request.setControls(prc);

      final SearchResult result = new SearchResult();
      byte[] cookie = null;
      do {
        prc.setCookie(cookie);

        final Response<SearchResult> response = search.execute(request);
        result.addEntries(response.getResult().getEntries());
        cookie = null;

        final PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
        if (ctl != null) {
          if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
            cookie = ctl.getCookie();
          }
        }
      } while (cookie != null);
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new SearchResultIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected),
          result));
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.UNAVAILABLE_CRITICAL_EXTENSION, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
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
      "virtualListViewSearchDn",
      "virtualListViewSearchFilter",
      "virtualListViewSearchReturnAttrs",
      "virtualListViewSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void virtualListViewSearch(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    // OracleDS returns protocol error
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    final SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("uugid", "caseExactMatch")}, true);
    VirtualListViewRequestControl vlvrc = new VirtualListViewRequestControl(3, 1, 1, true);
    byte[] contextID = null;
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      request.setControls(src, vlvrc);

      Response<SearchResult> response = search.execute(request);
      SearchResult result = response.getResult();
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new SearchResultIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected),
          result));
      contextID =
        ((VirtualListViewResponseControl) response.getControl(VirtualListViewResponseControl.OID)).getContextID();

      vlvrc = new VirtualListViewRequestControl("group4", 1, 1, contextID, true);
      request.setControls(src, vlvrc);
      response = search.execute(request);
      result = response.getResult();
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new SearchResultIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected),
          result));
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.UNAVAILABLE_CRITICAL_EXTENSION, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
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
      "sortSearchDn",
      "sortSearchFilter"
    }
  )
  @Test(groups = {"search"})
  public void sortedSearch(final String dn, final String filter)
    throws Exception
  {
    // OracleDS returns protocol error
    if (TestControl.isOracleDirectory()) {
      return;
    }

    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
      request.setSortBehavior(SortBehavior.ORDERED);

      // test sort by uugid
      SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("uugid", "caseExactMatch")}, true);
      request.setControls(src);

      SearchResult result = search.execute(request).getResult();

      // confirm sorted
      int i = 2;
      for (LdapEntry e : result.getEntries()) {
        AssertJUnit.assertEquals(String.valueOf(2000 + i), e.getAttribute("uid").getStringValue());
        i++;
      }

      // test sort by uid
      src = new SortRequestControl(new SortKey[] {new SortKey("uid", "integerMatch", true)}, true);
      request.setControls(src);
      result = search.execute(request).getResult();

      // confirm sorted
      i = 5;
      for (LdapEntry e : result.getEntries()) {
        AssertJUnit.assertEquals(String.valueOf(2000 + i), e.getAttribute("uid").getStringValue());
        i--;
      }
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.UNAVAILABLE_CRITICAL_EXTENSION, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  authzFrom  to proxy from
   * @param  authzTo  to proxy to
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "proxyAuthzFrom",
      "proxyAuthzTo",
      "proxyAuthzSearchDn",
      "proxyAuthzSearchFilter"
    }
  )
  @Test(groups = {"search"})
  public void proxyAuthzSearch(final String authzFrom, final String authzTo, final String dn, final String filter)
    throws Exception
  {
    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    final Connection conn = TestUtils.createSetupConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));

      // no authz
      Response<SearchResult> response = search.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(1, response.getResult().size());

      // anonymous authz
      request.setControls(new ProxyAuthorizationControl("dn:"));
      response = search.execute(request);
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION.equals(response.getResultCode())) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(0, response.getResult().size());

      // authz denied
      request.setControls(new ProxyAuthorizationControl("dn:" + authzTo));
      try {
        response = search.execute(request);
        AssertJUnit.assertEquals(ResultCode.AUTHORIZATION_DENIED, response.getResultCode());
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.AUTHORIZATION_DENIED, e.getResultCode());
      }

      // add authzTo
      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(
          authzFrom,
          new AttributeModification(AttributeModificationType.ADD, new LdapAttribute("authzTo", "dn:" + authzTo))));

      response = search.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(1, response.getResult().size());

    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.UNAVAILABLE_CRITICAL_EXTENSION, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
    } finally {
      try {
        // remove authzTo
        final ModifyOperation modify = new ModifyOperation(conn);
        modify.execute(
          new ModifyRequest(
            authzFrom,
            new AttributeModification(AttributeModificationType.REMOVE, new LdapAttribute("authzTo"))));
      } catch (LdapException e) {
        AssertJUnit.fail(e.getMessage());
      }
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "recursiveSearchDn",
      "recursiveSearchFilter",
      "recursiveSearchFilterParameters",
      "recursiveSearchReturnAttrs",
      "recursiveHandlerResults"
    }
  )
  @Test(groups = {"search"})
  public void recursiveHandlerSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveEntryHandler rsrh = new RecursiveEntryHandler("member", "uugid", "uid");

    final SearchRequest sr = new SearchRequest(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"));
    sr.setSearchEntryHandlers(rsrh);

    final SearchResult result = search.execute(sr).getResult();
    // ignore the case of member and contactPerson; some directories return
    // those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("member", "contactPerson")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        result.getEntry()));
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
      "recursiveSearch2Dn",
      "recursiveSearch2Filter",
      "recursiveSearch2ReturnAttrs",
      "recursiveHandlerResults2"
    }
  )
  @Test(groups = {"search"})
  public void recursiveHandlerSearch2(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveEntryHandler rsrh = new RecursiveEntryHandler("member", "member");

    final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
    sr.setSearchEntryHandlers(rsrh);

    final SearchResult result = search.execute(sr).getResult();
    // ignore the case of member and contactPerson; some directories return
    // those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("member")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        result.getEntry()));
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
      "mergeSearchDn",
      "mergeSearchFilter",
      "mergeSearchReturnAttrs",
      "mergeSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void mergeSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test result merge
      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      sr.setSortBehavior(SortBehavior.SORTED);

      final SearchResult result = search.execute(sr).getResult();
      // ignore the case of member and contactPerson; some directories return
      // those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new LdapEntryIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected).getEntry(),
          SearchResult.mergeEntries(result).getEntry()));
    } finally {
      conn.close();
    }
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
      "mergeDuplicateSearchDn",
      "mergeDuplicateSearchFilter",
      "mergeDuplicateReturnAttrs",
      "mergeDuplicateSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void mergeDuplicateSearch(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test result merge
      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      sr.setSortBehavior(SortBehavior.SORTED);

      final SearchResult result = search.execute(sr).getResult();
      // ignore the case of member and contactPerson; some directories return
      // those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new LdapEntryIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected).getEntry(),
          SearchResult.mergeEntries(result).getEntry()));
    } finally {
      conn.close();
    }
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
      "mergeAttributeSearchDn",
      "mergeAttributeSearchFilter",
      "mergeAttributeReturnAttrs",
      "mergeAttributeSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void mergeAttributeSearch(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test merge searching
      final MergeAttributeEntryHandler handler = new MergeAttributeEntryHandler();
      handler.setMergeAttributeName("cn");
      handler.setAttributeNames("displayName", "givenName", "sn");

      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(handler);
      sr.setSortBehavior(SortBehavior.SORTED);

      final SearchResult result = search.execute(sr).getResult();
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttr  to return from search.
   * @param  base64Value  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "binarySearchDn",
      "binarySearchFilter",
      "binarySearchReturnAttr",
      "binarySearchResult"
    }
  )
  @Test(groups = {"search"})
  public void binarySearch(final String dn, final String filter, final String returnAttr, final String base64Value)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));

    // test binary searching
    SearchRequest request = new SearchRequest(dn, new SearchFilter(filter), returnAttr);
    request.setBinaryAttributes(returnAttr);

    SearchResult result = search.execute(request).getResult();
    AssertJUnit.assertTrue(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertEquals(base64Value, result.getEntry().getAttribute().getStringValue());

    request = new SearchRequest(dn, new SearchFilter(filter), "sn");
    result = search.execute(request).getResult();
    AssertJUnit.assertFalse(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute().getBinaryValue());

    request = new SearchRequest(dn, new SearchFilter(filter), "sn");
    request.setBinaryAttributes("sn");
    result = search.execute(request).getResult();
    AssertJUnit.assertTrue(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute().getBinaryValue());

    request = new SearchRequest(dn, new SearchFilter(filter), "userCertificate;binary");
    result = search.execute(request).getResult();
    AssertJUnit.assertTrue(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute().getBinaryValue());
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterParameters",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(groups = {"search"})
  public void caseChangeSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final CaseChangeEntryHandler srh = new CaseChangeEntryHandler();
      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test no case change
      final SearchResult noChangeResult = TestUtils.convertLdifToResult(expected);
      SearchRequest sr = new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")),
        returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(srh);

      SearchResult result = search.execute(sr).getResult();
      TestUtils.assertEquals(noChangeResult, result);

      // test lower case attribute values
      srh.setAttributeValueCaseChange(CaseChange.LOWER);

      final SearchResult lcValuesChangeResult = TestUtils.convertLdifToResult(expected);
      for (LdapAttribute la : lcValuesChangeResult.getEntry().getAttributes()) {
        final Set<String> s = new HashSet<>();
        for (String value : la.getStringValues()) {
          s.add(value.toLowerCase());
        }
        la.clear();
        la.addStringValues(s);
      }
      sr = new SearchRequest(dn, new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(srh);
      result = search.execute(sr).getResult();
      TestUtils.assertEquals(lcValuesChangeResult, result);

      // test upper case attribute names
      srh.setAttributeValueCaseChange(CaseChange.NONE);
      srh.setAttributeNameCaseChange(CaseChange.UPPER);

      final SearchResult ucNamesChangeResult = TestUtils.convertLdifToResult(expected);
      for (LdapAttribute la : ucNamesChangeResult.getEntry().getAttributes()) {
        la.setName(la.getName().toUpperCase());
      }
      sr = new SearchRequest(dn, new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(srh);
      result = search.execute(sr).getResult();
      TestUtils.assertEquals(ucNamesChangeResult, result);

      // test lower case everything
      srh.setAttributeValueCaseChange(CaseChange.LOWER);
      srh.setAttributeNameCaseChange(CaseChange.LOWER);
      srh.setDnCaseChange(CaseChange.LOWER);

      final SearchResult lcAllChangeResult = TestUtils.convertLdifToResult(expected);
      for (LdapAttribute la : ucNamesChangeResult.getEntry().getAttributes()) {
        lcAllChangeResult.getEntry().setDn(lcAllChangeResult.getEntry().getDn().toLowerCase());
        la.setName(la.getName().toLowerCase());

        final Set<String> s = new HashSet<>();
        for (String value : la.getStringValues()) {
          s.add(value.toLowerCase());
        }
        la.clear();
        la.addStringValues(s);
      }
      sr = new SearchRequest(dn, new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(srh);
      result = search.execute(sr).getResult();
      TestUtils.assertEquals(ucNamesChangeResult, result);
    } finally {
      conn.close();
    }
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
      "rangeSearchDn",
      "rangeSearchFilter",
      "rangeSearchReturnAttrs",
      "rangeHandlerResults"
    }
  )
  @Test(groups = {"search"})
  public void rangeHandlerSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter), returnAttrs.split("\\|"));
      sr.setSearchEntryHandlers(new RangeEntryHandler(), new ObjectSidHandler(), new ObjectGuidHandler());

      final SearchResult result = search.execute(sr).getResult();
      // ignore the case of member; some directories return it in mixed case
      AssertJUnit.assertEquals(
        0,
        (new LdapEntryIgnoreCaseComparator("member")).compare(
          TestUtils.convertLdifToResult(expected).getEntry(),
          result.getEntry()));
    } catch (LdapException e) {
      // some providers don't support this DN syntax
      AssertJUnit.assertEquals(ResultCode.DECODING_ERROR, e.getResultCode());
    } finally {
      conn.close();
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
      "statsSearchDn",
      "statsSearchFilter"
    }
  )
  @Test(groups = {"search"})
  public void getStatsSearch(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter));
      sr.setSearchEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
      sr.setControls(new GetStatsControl());

      final Response<SearchResult> response = search.execute(sr);
      final GetStatsControl ctrl = (GetStatsControl) response.getControl(GetStatsControl.OID);
      AssertJUnit.assertTrue(ctrl.getStatistics().size() > 1);

      final LdapAttribute whenCreated = response.getResult().getEntry().getAttribute("whenCreated");
      AssertJUnit.assertNotNull(whenCreated.getValue(new GeneralizedTimeValueTranscoder()));

      final LdapAttribute whenChanged = response.getResult().getEntry().getAttribute("whenChanged");
      AssertJUnit.assertNotNull(whenChanged.getValue(new GeneralizedTimeValueTranscoder()));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  host  for verify name
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "miscADControlsHost",
      "miscADControlsDn",
      "miscADControlsFilter"
    }
  )
  @Test(groups = {"search"})
  public void miscADControlsSearch(final String host, final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    // provider doesn't support this control
    if (TestControl.isApacheProvider()) {
      return;
    }

    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchRequest sr = new SearchRequest(dn, new SearchFilter(filter));
      sr.setSearchEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
      sr.setControls(
        new ForceUpdateControl(),
        new LazyCommitControl(),
        /*
         * new NotificationControl());
         * new VerifyNameControl(host));
         */
        new PermissiveModifyControl(),
        new RangeRetrievalNoerrControl(),
        new SearchOptionsControl(),
        new ShowDeactivatedLinkControl(),
        new ShowDeletedControl(),
        new ShowRecycledControl());
      search.execute(sr);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  binaryFilter  to search for binary attributes.
   * @param  binaryFilterParameters  to replace parameters in binary filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "specialCharSearchDn",
      "specialCharSearchFilter",
      "specialCharSearchFilterParameters",
      "specialCharBinarySearchFilter",
      "specialCharBinarySearchFilterParameters",
      "specialCharReturnAttrs",
      "specialCharSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void specialCharsSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String binaryFilter,
    final String binaryFilterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createLdapConnection(false));
    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResult specialCharsResult = TestUtils.convertLdifToResult(expected);

    SearchResult result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
    // DNs returned from JNDI may have escaped characters
    result.getEntry().setDn(result.getEntry().getDn().replaceAll("\\\\", ""));
    TestUtils.assertEquals(specialCharsResult, result);

    result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(
          binaryFilter,
          new Object[] {LdapUtils.base64Decode(binaryFilterParameters)}),
        returnAttrs.split("\\|"))).getResult();
    // DNs returned from JNDI may have escaped characters
    result.getEntry().setDn(result.getEntry().getDn().replaceAll("\\\\", ""));
    TestUtils.assertEquals(specialCharsResult, result);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "rewriteSearchDn",
      "rewriteSearchFilter",
      "rewriteSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void rewriteSearch(final String dn, final String filter, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final Connection conn = createLdapConnection(true);
    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResult specialCharsResult = TestUtils.convertLdifToResult(expected);
    specialCharsResult.getEntry().setDn(specialCharsResult.getEntry().getDn().replaceAll("\\\\", ""));

    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      // test special character searching
      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
      final SearchResult result = search.execute(request).getResult();
      TestUtils.assertEquals(specialCharsResult, result);
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  resultsSize  of search results.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchExceededDn",
      "searchExceededFilter",
      "searchExceededResultsSize"
    }
  )
  @Test(groups = {"search"})
  public void searchExceeded(final String dn, final String filter, final int resultsSize)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchRequest request = new SearchRequest();
      request.setBaseDn(dn);
      request.setSizeLimit(resultsSize);

      request.setSearchFilter(new SearchFilter("(uugid=*)"));

      Response<SearchResult> response = search.execute(request);
      AssertJUnit.assertEquals(resultsSize, response.getResult().size());
      AssertJUnit.assertEquals(ResultCode.SIZE_LIMIT_EXCEEDED, response.getResultCode());

      request.setSearchFilter(new SearchFilter(filter));
      response = search.execute(request);
      AssertJUnit.assertEquals(resultsSize, response.getResult().size());
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    } finally {
      conn.close();
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
      "searchReferralDn",
      "searchReferralFilter"
    }
  )
  @Test(groups = {"search"})
  public void searchReferral(final String dn, final String filter)
    throws Exception
  {
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final Connection conn = createLdapConnection(true);

    // expects a referral on the dn ou=referrals
    final String referralDn = "ou=referrals," + DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setSearchFilter(new SearchFilter(filter));

    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      try {
        final Response<SearchResult> response = search.execute(request);
        AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
        AssertJUnit.assertTrue(response.getReferralURLs().length > 0);
        for (String s : response.getReferralURLs()) {
          AssertJUnit.assertTrue(response.getReferralURLs()[0].startsWith(conn.getConnectionConfig().getLdapUrl()));
        }
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.REFERRAL, e.getResultCode());
        AssertJUnit.assertTrue(e.getReferralURLs().length > 0);
        for (String s : e.getReferralURLs()) {
          AssertJUnit.assertTrue(e.getReferralURLs()[0].startsWith(conn.getConnectionConfig().getLdapUrl()));
        }
      }
    } finally {
      conn.close();
    }

    request.setReferralHandler(new SearchReferralHandler());
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      try {
        final Response<SearchResult> response = search.execute(request);
        if (response.getResultCode() == ResultCode.SUCCESS) {
          AssertJUnit.assertTrue(response.getResult().size() > 0);
        } else {
          // some providers don't support authenticated referrals
          AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
        }
      } catch (UnsupportedOperationException e) {
        // ignore this test if not supported
        AssertJUnit.assertNotNull(e);
      }
    } finally {
      conn.close();
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
      "searchReferenceDn",
      "searchReferenceFilter"
    }
  )
  @Test(groups = {"search"})
  public void searchReference(final String dn, final String filter)
    throws Exception
  {
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final Connection conn = createLdapConnection(true);
    final List<SearchReference> refs = new ArrayList<>();

    // expects a referral on the root dn
    final String referralDn = DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setSearchFilter(new SearchFilter(filter));
    request.setSearchReferenceHandlers(
      new SearchReferenceHandler() {
        @Override
        public HandlerResult<SearchReference> handle(
          final Connection conn,
          final SearchRequest request,
          final SearchReference reference)
          throws LdapException
        {
          refs.add(reference);
          return new HandlerResult<>(reference);
        }

        @Override
        public void initializeRequest(final SearchRequest request) {}
      });

    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final Response<SearchResult> response = search.execute(request);
      AssertJUnit.assertTrue(response.getResult().size() > 0);
      // some providers don't support search references
      // in that case URLs are provided on the response
      if (refs.size() > 0) {
        AssertJUnit.assertTrue(refs.size() > 0);
        for (SearchReference r : refs) {
          AssertJUnit.assertNotNull(r.getReferralUrls());
          for (String s : r.getReferralUrls()) {
            AssertJUnit.assertNotNull(s);
          }
        }
      } else {
        AssertJUnit.assertTrue(response.getReferralURLs().length > 0);
        for (String s : response.getReferralURLs()) {
          AssertJUnit.assertTrue(response.getReferralURLs()[0].startsWith(conn.getConnectionConfig().getLdapUrl()));
        }
      }
      // providers may return either result code
      if (response.getResultCode() != ResultCode.SUCCESS && response.getResultCode() != ResultCode.REFERRAL) {
        AssertJUnit.fail("Invalid result code: " + response);
      }
    } finally {
      conn.close();
    }

    refs.clear();
    request.setReferralHandler(new SearchReferralHandler(3));
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      try {
        final Response<SearchResult> response = search.execute(request);
        // providers may return either result code
        if (
          response.getResultCode() != ResultCode.SUCCESS &&
            response.getResultCode() != ResultCode.REFERRAL &&
            response.getResultCode() != ResultCode.PARTIAL_RESULTS) {
          AssertJUnit.fail("Invalid result code: " + response);
        }
        AssertJUnit.assertTrue(response.getResult().size() > 0);
      } catch (LdapException e) {
        if (e.getCause() instanceof UnsupportedOperationException) {
          // ignore this test if not supported
          AssertJUnit.assertNotNull(e);
        } else {
          AssertJUnit.assertEquals(ResultCode.REFERRAL, e.getResultCode());
        }
      } catch (UnsupportedOperationException e) {
        // ignore this test if not supported
        AssertJUnit.assertNotNull(e);
      }
    } finally {
      conn.close();
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
      "searchActiveDirectoryDn",
      "searchActiveDirectoryFilter"
    }
  )
  @Test(groups = {"search"})
  public void searchActiveDirectory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final Connection conn = createLdapConnection(true);
    final List<SearchReference> refs = new ArrayList<>();

    // expects a referral on the root dn
    final String referralDn = DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setSearchFilter(new SearchFilter(filter));
    request.setSearchEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler(), new PrimaryGroupIdHandler());
    request.setSearchReferenceHandlers(
      new SearchReferenceHandler() {
        @Override
        public HandlerResult<SearchReference> handle(
          final Connection conn,
          final SearchRequest request,
          final SearchReference reference)
          throws LdapException
        {
          refs.add(reference);
          return new HandlerResult<>(reference);
        }

        @Override
        public void initializeRequest(final SearchRequest request) {}
      });

    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final Response<SearchResult> response = search.execute(request);
      AssertJUnit.assertTrue(response.getResult().size() > 0);
      // some providers don't support search references
      // in that case URLs are provided on the response
      if (refs.size() > 0) {
        AssertJUnit.assertTrue(refs.size() > 0);
        for (SearchReference r : refs) {
          AssertJUnit.assertNotNull(r.getReferralUrls());
          for (String s : r.getReferralUrls()) {
            AssertJUnit.assertNotNull(s);
          }
        }
      } else {
        AssertJUnit.assertTrue(response.getReferralURLs().length > 0);
        for (String s : response.getReferralURLs()) {
          AssertJUnit.assertNotNull(s);
        }
      }
      // AssertJUnit.assertTrue(response.getReferralURLs().length > 0);
      // providers may return either result code
      if (response.getResultCode() != ResultCode.SUCCESS && response.getResultCode() != ResultCode.REFERRAL) {
        AssertJUnit.fail("Invalid result code: " + response);
      }
    } finally {
      conn.close();
    }

    refs.clear();
    request.setReferralHandler(new SearchReferralHandler());
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      try {
        final Response<SearchResult> response = search.execute(request);
        AssertJUnit.assertTrue(response.getResult().size() > 0);
        // AssertJUnit.assertNull(response.getReferralURLs());
        // AD referrals cannot be followed
        // providers may return either result code
        if (response.getResultCode() != ResultCode.SUCCESS && response.getResultCode() != ResultCode.PARTIAL_RESULTS) {
          AssertJUnit.fail("Invalid result code: " + response);
        }
      } catch (LdapException e) {
        // some providers throw referral exception here
        AssertJUnit.assertEquals(ResultCode.REFERRAL, e.getResultCode());
      } catch (UnsupportedOperationException e) {
        // ignore this test if referrals not supported
        AssertJUnit.assertNotNull(e);
      }
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  resultCode  to retry operations on.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchRetryDn",
      "searchRetryResultCode"
    }
  )
  @Test(groups = {"search-with-retry"}, dependsOnGroups = {"search"})
  public void searchWithRetry(final String dn, final String resultCode)
    throws Exception
  {
    final ResultCode retryResultCode = ResultCode.valueOf(resultCode);
    final ConnectionConfig cc = TestUtils.readConnectionConfig(null);
    DefaultConnectionFactory cf = new DefaultConnectionFactory(cc);
    cf.getProvider().getProviderConfig().setOperationExceptionResultCodes(retryResultCode);

    Connection conn = cf.getConnection();
    RetrySearchOperation search = new RetrySearchOperation(
      conn,
      new LdapException("Retry search exception", ResultCode.NO_SUCH_OBJECT));

    try {
      conn.open();

      // test no retry
      search.setAllowRetry(false);
      try {
        final Response<SearchResult> response = search.execute(
          new SearchRequest(dn, new SearchFilter("(objectclass=*)")));
        AssertJUnit.fail("Should have thrown LdapException, returned: " + response);
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      }
      AssertJUnit.assertEquals(0, search.getRetryCount());
      AssertJUnit.assertEquals(0, search.getRunTime());
    } finally {
      conn.close();
    }

    // test no exception
    search.setAllowRetry(true);
    cf = new DefaultConnectionFactory(cc);
    cf.getProvider().getProviderConfig().setOperationExceptionResultCodes((ResultCode[]) null);
    conn = cf.getConnection();
    search = new RetrySearchOperation(conn, new LdapException("Retry search exception", ResultCode.NO_SUCH_OBJECT));

    try {
      conn.open();
      try {
        final Response<SearchResult> response = search.execute(
          new SearchRequest(dn, new SearchFilter("(objectclass=*)")));
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, response.getResultCode());
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      }
      AssertJUnit.assertEquals(0, search.getRetryCount());
      AssertJUnit.assertEquals(0, search.getRunTime());
    } finally {
      conn.close();
    }

    // test retry count and wait time
    cf = new DefaultConnectionFactory(cc);
    cf.getProvider().getProviderConfig().setOperationExceptionResultCodes(retryResultCode);
    conn = cf.getConnection();
    search = new RetrySearchOperation(conn, new LdapException("Retry search exception", ResultCode.NO_SUCH_OBJECT));
    search.setReopenRetry(3);
    search.setReopenRetryWait(1);

    try {
      conn.open();
      try {
        final Response<SearchResult> response = search.execute(
          new SearchRequest(dn, new SearchFilter("(objectclass=*)")));
        AssertJUnit.fail("Should have thrown LdapException, returned: " + response);
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      }
      AssertJUnit.assertEquals(3, search.getRetryCount());
      AssertJUnit.assertTrue(search.getRunTime() > 0);

      // test backoff interval
      search.reset();
      search.setReopenRetryBackoff(2);
      try {
        final Response<SearchResult> response = search.execute(
          new SearchRequest(dn, new SearchFilter("(objectclass=*)")));
        AssertJUnit.fail("Should have thrown LdapException, returned: " + response);
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      }
      AssertJUnit.assertEquals(3, search.getRetryCount());
      AssertJUnit.assertTrue(search.getRunTime() > 0);

      // test infinite retries
      search.reset();
      search.setStopCount(10);
      search.setReopenRetry(-1);
      try {
        final Response<SearchResult> response = search.execute(
          new SearchRequest(dn, new SearchFilter("(objectclass=*)")));
        AssertJUnit.fail("Should have thrown LdapException, returned: " + response);
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.NO_SUCH_OBJECT, e.getResultCode());
      }
      AssertJUnit.assertEquals(10, search.getRetryCount());
      AssertJUnit.assertTrue(search.getRunTime() > 0);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesDn",
      "getAttributesReturnAttrs",
      "getAttributesResults"
    }
  )
  @Test(
    groups = {"search"}, threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void getAttributes(final String dn, final String returnAttrs, final String results)
    throws Exception
  {
    final Connection conn = createLdapConnection(false);
    final SearchOperation search = new SearchOperation(conn);
    final SearchResult result = search.execute(
      SearchRequest.newObjectScopeSearchRequest(dn, returnAttrs.split("\\|"))).getResult();
    TestUtils.assertEquals(TestUtils.convertStringToEntry(dn, results), result.getEntry());
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "getAttributesBase64Dn",
      "getAttributesBase64ReturnAttrs",
      "getAttributesBase64Results"
    }
  )
  @Test(groups = {"search"})
  public void getAttributesBase64(final String dn, final String returnAttrs, final String results)
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(dn, returnAttrs.split("\\|"));
      request.setBinaryAttributes("jpegPhoto");

      final SearchResult result = search.execute(request).getResult();
      AssertJUnit.assertEquals(
        TestUtils.convertStringToEntry(dn, results).getAttribute("jpegPhoto").getStringValue(),
        result.getEntry().getAttribute("jpegPhoto").getStringValue());
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"search"})
  public void getSaslMechanisms()
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest("", new String[] {"supportedSASLMechanisms"})).getResult();
      AssertJUnit.assertTrue(result.getEntry().getAttributes().size() > 0);
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = {"search"})
  public void getSupportedControls()
    throws Exception
  {
    final Connection conn = createLdapConnection(true);
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest("", new String[] {"supportedcontrol"})).getResult();
      AssertJUnit.assertTrue(result.getEntry().getAttributes().size() > 0);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "digestMd5SearchDn",
      "digestMd5SearchFilter",
      "digestMd5SearchFilterParameters",
      "digestMd5SearchReturnAttrs",
      "digestMd5SearchResults"
    }
  )
  @Test(groups = {"search"})
  public void digestMd5Search(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final Connection conn = TestUtils.createDigestMd5Connection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "cramMd5SearchDn",
      "cramMd5SearchFilter",
      "cramMd5SearchFilterParameters",
      "cramMd5SearchReturnAttrs",
      "cramMd5SearchResults"
    }
  )
  @Test(groups = {"search"})
  public void cramMd5Search(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final Connection conn = TestUtils.createCramMd5Connection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (LdapException e) {
      // ignore this test if not supported by the server
      AssertJUnit.assertEquals(ResultCode.AUTH_METHOD_NOT_SUPPORTED, e.getResultCode());
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the provider
      AssertJUnit.assertNotNull(e);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "saslExternalSearchDn",
      "saslExternalSearchFilter",
      "saslExternalSearchFilterParameters",
      "saslExternalSearchReturnAttrs",
      "saslExternalSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void saslExternalSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // TODO ignore active directory until it's configured
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    Connection conn = null;
    try {
      conn = TestUtils.createSaslExternalConnection();
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }


  /**
   * @param  krb5Realm  kerberos realm
   * @param  krb5Kdc  kerberos kdc
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "krb5Realm",
      "ldapTestHost",
      "gssApiSearchDn",
      "gssApiSearchFilter",
      "gssApiSearchFilterParameters",
      "gssApiSearchReturnAttrs",
      "gssApiSearchResults"
    }
  )
  @Test(groups = {"search"})
  public void gssApiSearch(
    final String krb5Realm,
    final String krb5Kdc,
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    // ignore directory until it's configured
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final LdapURL ldapUrl = new LdapURL(krb5Kdc);
    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    System.setProperty("java.security.krb5.realm", krb5Realm);
    System.setProperty("java.security.krb5.kdc", ldapUrl.getEntry().getHostname());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final Connection conn = TestUtils.createGssApiConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final SearchResult result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"))).getResult();
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      System.clearProperty("java.security.auth.login.config");
      System.clearProperty("javax.security.auth.useSubjectCredsOnly");
      System.clearProperty("java.security.krb5.realm");
      System.clearProperty("java.security.krb5.kdc");
      conn.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchDn",
      "searchFilter",
      "searchFilterParameters",
      "searchReturnAttrs",
      "searchResults"
    }
  )
  @Test(groups = {"search"})
  public void executorSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchExecutor executor = new SearchExecutor();
    executor.setBaseDn(dn);
    executor.setSearchFilter(new SearchFilter(filter, filterParameters.split("\\|")));
    executor.setReturnAttributes(returnAttrs.split("\\|"));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final ConnectionFactory cf = new DefaultConnectionFactory(TestUtils.readConnectionConfig(null));
    SearchResult result = executor.search(cf).getResult();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    BlockingConnectionPool pool = new BlockingConnectionPool(
      new DefaultConnectionFactory(TestUtils.readConnectionConfig(null)));
    pool.setConnectOnCreate(false);
    pool.initialize();

    PooledConnectionFactory pcf = new PooledConnectionFactory(pool);
    result = executor.search(pcf).getResult();
    pool.close();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    pool = new BlockingConnectionPool(new DefaultConnectionFactory(TestUtils.readConnectionConfig(null)));
    pool.setConnectOnCreate(true);
    pool.initialize();
    pcf = new PooledConnectionFactory(pool);
    result = executor.search(pcf).getResult();
    pool.close();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
  }
}
