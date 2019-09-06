/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.ldaptive.concurrent.SearchOperationWorker;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.ProxyAuthorizationControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.control.VirtualListViewRequestControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.CaseChangeEntryHandler.CaseChange;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.NoOpEntryHandler;
import org.ldaptive.handler.RecursiveResultHandler;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.referral.DefaultReferralConnectionFactory;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
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
    })
  @BeforeClass(groups = "search", dependsOnGroups = "searchInit")
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
    final ModifyOperation modify = new ModifyOperation(TestUtils.createSetupConnectionFactory());
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("2")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("member", "cn=Group 3," + baseDn))));
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("3")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("member", "cn=Group 4," + baseDn, "cn=Group 5," + baseDn))));
    modify.execute(
      new ModifyRequest(
        GROUP_ENTRIES.get("4")[0].getDn(),
        new AttributeModification(
          AttributeModification.Type.ADD,
          new LdapAttribute("member", "cn=Group 2," + baseDn, "cn=Group 3," + baseDn))));
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry")
  @BeforeClass(groups = "search")
  public void createSpecialCharsEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    specialCharsLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "search")
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
    })
  @Test(
    groups = "search", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void search(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final SearchResponse entryDnResult = TestUtils.convertLdifToResult(expected);
    entryDnResult.getEntry().addAttributes(new LdapAttribute("entryDN", entryDnResult.getEntry().getDn()));

    // test searching
    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter, filterParameters.split("\\|")))
         .attributes(returnAttrs.split("\\|")).build());
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    // test searching no attributes
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter, filterParameters.split("\\|")))
        .attributes(ReturnAttributes.NONE.value()).build());
    AssertJUnit.assertTrue(result.getEntry().getAttributes().isEmpty());

    // test searching without handler
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new LdapEntryHandler[0]);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    // test searching with multiple handlers
    final DnAttributeEntryHandler srh = new DnAttributeEntryHandler();
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new NoOpEntryHandler(), srh);
    // ignore the case of entryDN; some directories return those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("entryDN")).compare(entryDnResult.getEntry(), result.getEntry()));

    // test that entry dn handler is no-op if attribute name conflicts
    srh.setDnAttributeName("givenName");
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new NoOpEntryHandler(), srh);
    // ignore the case of entryDN; some directories return those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("entryDN")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        result.getEntry()));
  }


  /**
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "searchFilter",
      "searchFilterParameters",
      "searchReturnAttrs",
      "searchResults"
    })
  @Test(groups = "search")
  public void searchScopes(
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchResponse expectedResult = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile));
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final SearchRequest subtreeRequest = SearchRequest.builder()
      .dn(DnParser.substring(expectedResult.getEntry().getDn(), 2))
      .filter(new SearchFilter(filter, filterParameters.split("\\|")))
      .attributes(returnAttrs.split("\\|"))
      .scope(SearchScope.SUBTREE).build();
    SearchResponse result = search.execute(subtreeRequest);
    TestUtils.assertEquals(expectedResult, result);

    final SearchRequest onelevelRequest = SearchRequest.builder()
      .dn(DnParser.substring(expectedResult.getEntry().getDn(), 1))
      .filter(new SearchFilter(filter, filterParameters.split("\\|")))
      .attributes(returnAttrs.split("\\|"))
      .scope(SearchScope.ONELEVEL).build();
    result = search.execute(onelevelRequest);
    TestUtils.assertEquals(expectedResult, result);

    final SearchRequest objectRequest = SearchRequest.builder()
      .dn(expectedResult.getEntry().getDn())
      .filter(new SearchFilter("(objectClass=*)"))
      .attributes(returnAttrs.split("\\|"))
      .scope(SearchScope.OBJECT).build();
    result = search.execute(objectRequest);
    TestUtils.assertEquals(expectedResult, result);
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
    })
  @Test(groups = "search")
  public void returnAttributesSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test searching, no attributes
    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter, filterParameters.split("\\|")))
        .attributes(ReturnAttributes.NONE.value()).build());
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertTrue(result.getEntry().getAttributes().isEmpty());

    // test searching, user attributes
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter, filterParameters.split("\\|")))
        .attributes(ReturnAttributes.ALL_USER.value()).build());
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("cn"));
    AssertJUnit.assertNull(result.getEntry().getAttribute("createTimestamp"));

    // test searching, operations attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new SearchFilter(filter, filterParameters.split("\\|")))
          .attributes(ReturnAttributes.ALL_OPERATIONAL.add("createTimestamp")).build());
    } else {
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new SearchFilter(filter, filterParameters.split("\\|")))
          .attributes(ReturnAttributes.ALL_OPERATIONAL.value()).build());
    }
    AssertJUnit.assertNotNull(result.getEntry());
    AssertJUnit.assertNull(result.getEntry().getAttribute("cn"));
    AssertJUnit.assertNotNull(result.getEntry().getAttribute("createTimestamp"));

    // test searching, all attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new SearchFilter(filter, filterParameters.split("\\|")))
          .attributes(ReturnAttributes.ALL.add("createTimestamp")).build());
    } else {
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new SearchFilter(filter, filterParameters.split("\\|")))
          .attributes(ReturnAttributes.ALL.value()).build());
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
    })
  @Test(groups = "search")
  public void pagedSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final SingleConnectionFactory cf = TestUtils.createSingleConnectionFactory();
    final PagedResultsControl prc = new PagedResultsControl(1, true);
    try {
      final SearchOperation search = new SearchOperation(cf);
      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter))
        .attributes(returnAttrs.split("\\|"))
        .controls(prc).build();

      final SearchResponse pagedResults = new SearchResponse();
      byte[] cookie = null;
      do {
        prc.setCookie(cookie);

        final SearchResponse response = search.execute(request);
        if (response.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
          // ignore this test if not supported by the server
          throw new UnsupportedOperationException("LDAP server does not support this control");
        }
        pagedResults.addEntries(response.getEntries());
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
          pagedResults));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      cf.close();
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
    })
  @Test(groups = "search")
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

    final SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("uugid", "caseExactMatch")}, true);
    VirtualListViewRequestControl vlvrc = new VirtualListViewRequestControl(3, 1, 1, true);
    final byte[] contextID;
    try {
      final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
      final String expected = TestUtils.readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter))
        .attributes(returnAttrs.split("\\|"))
        .controls(src, vlvrc).build();

      SearchResponse result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new SearchResultIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected),
          result));
      contextID =
        ((VirtualListViewResponseControl) result.getControl(VirtualListViewResponseControl.OID)).getContextID();

      vlvrc = new VirtualListViewRequestControl("group4", 1, 1, contextID, true);
      request.setControls(src, vlvrc);
      result = search.execute(request);
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      AssertJUnit.assertEquals(
        0,
        (new SearchResultIgnoreCaseComparator("member", "contactPerson")).compare(
          TestUtils.convertLdifToResult(expected),
          result));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
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
    })
  @Test(groups = "search")
  public void sortedSearch(final String dn, final String filter)
    throws Exception
  {
    // OracleDS returns protocol error
    if (TestControl.isOracleDirectory()) {
      return;
    }

    try {
      final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

      SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("uugid", "caseExactMatch")}, true);

      // test sort by uugid
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter))
        .controls(src).build();
      SearchResponse result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }

      // confirm sorted
      int i = 2;
      for (LdapEntry e : result.getEntries()) {
        AssertJUnit.assertEquals(String.valueOf(2000 + i), e.getAttribute("uid").getStringValue());
        i++;
      }

      // test sort by uid
      src = new SortRequestControl(new SortKey[] {new SortKey("uid", "integerMatch", true)}, true);
      request.setControls(src);
      result = search.execute(request);

      // confirm sorted
      i = 5;
      for (LdapEntry e : result.getEntries()) {
        AssertJUnit.assertEquals(String.valueOf(2000 + i), e.getAttribute("uid").getStringValue());
        i--;
      }
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      AssertJUnit.assertNotNull(e);
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
    })
  @Test(groups = "search")
  public void proxyAuthzSearch(final String authzFrom, final String authzTo, final String dn, final String filter)
    throws Exception
  {
    boolean addedAttribute = false;
    try {
      final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter)).build();

      // no authz
      SearchResponse response = search.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(1, response.entrySize());

      // anonymous authz
      request.setControls(new ProxyAuthorizationControl("dn:"));
      response = search.execute(request);
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION == response.getResultCode()) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(0, response.entrySize());

      // authz denied
      request.setControls(new ProxyAuthorizationControl("dn:" + authzTo));
      response = search.execute(request);
      AssertJUnit.assertEquals(ResultCode.AUTHORIZATION_DENIED, response.getResultCode());

      // add authzTo
      final ModifyOperation modify = new ModifyOperation(TestUtils.createConnectionFactory());
      modify.execute(
        new ModifyRequest(
          authzFrom,
          new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("authzTo", "dn:" + authzTo))));
      addedAttribute = true;

      response = search.execute(request);
      AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
      AssertJUnit.assertEquals(1, response.entrySize());

    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      AssertJUnit.assertNotNull(e);
    } finally {
      if (addedAttribute) {
        try {
          // remove authzTo
          final ModifyOperation modify = new ModifyOperation(TestUtils.createConnectionFactory());
          modify.execute(
            new ModifyRequest(
              authzFrom,
              new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("authzTo"))));
        } catch (LdapException e) {
          AssertJUnit.fail(e.getMessage());
        }
      }
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
    })
  @Test(groups = "search")
  public void recursiveHandlerSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveResultHandler rsrh = new RecursiveResultHandler("member", "uugid", "uid");
    search.setSearchResultHandlers(rsrh);

    final SearchResponse result = search.execute(
      dn, new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"));

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
    })
  @Test(groups = "search")
  public void recursiveHandlerSearch2(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveResultHandler rsrh = new RecursiveResultHandler("member", "member");
    search.setSearchResultHandlers(rsrh);

    final SearchResponse result = search.execute(dn, new SearchFilter(filter), returnAttrs.split("\\|"));

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
    })
  @Test(groups = "search")
  public void mergeSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test result merge
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(new SearchFilter(filter))
      .attributes(returnAttrs.split("\\|")).build();

    final SearchResponse result = search.execute(sr);
    // ignore the case of member and contactPerson; some directories return
    // those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("member", "contactPerson")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        SearchResponse.merge(result).getEntry()));
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
    })
  @Test(groups = "search")
  public void mergeDuplicateSearch(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test result merge
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(new SearchFilter(filter))
      .attributes(returnAttrs.split("\\|")).build();

    final SearchResponse result = search.execute(sr);
    // ignore the case of member and contactPerson; some directories return
    // those in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("member", "contactPerson")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        SearchResponse.merge(result).getEntry()));
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
    })
  @Test(groups = "search")
  public void mergeAttributeSearch(
    final String dn,
    final String filter,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test merge searching
    final MergeAttributeEntryHandler handler = new MergeAttributeEntryHandler();
    handler.setMergeAttributeName("cn");
    handler.setAttributeNames("displayName", "givenName", "sn");

    final SearchResponse result = search.execute(
      dn,
      new SearchFilter(filter),
      returnAttrs.split("\\|"),
      handler);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
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
    })
  @Test(groups = "search")
  public void binarySearch(final String dn, final String filter, final String returnAttr, final String base64Value)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    // test binary searching
    SearchRequest request = SearchRequest.builder()
      .dn(dn)
      .filter(new SearchFilter(filter))
      .attributes(returnAttr)
      .binary(returnAttr).build();

    SearchResponse result = search.execute(request);
    AssertJUnit.assertTrue(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertEquals(base64Value, result.getEntry().getAttribute().getStringValue());

    request = SearchRequest.builder().dn(dn).filter(new SearchFilter(filter)).attributes("sn").build();
    result = search.execute(request);
    AssertJUnit.assertFalse(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute().getBinaryValue());

    request = SearchRequest.builder().dn(dn).filter(new SearchFilter(filter)).attributes("sn").binary("sn").build();
    result = search.execute(request);
    AssertJUnit.assertTrue(result.getEntry().getAttribute().isBinary());
    AssertJUnit.assertNotNull(result.getEntry().getAttribute().getBinaryValue());

    request = SearchRequest.builder()
      .dn(dn)
      .filter(new SearchFilter(filter))
      .attributes("userCertificate;binary").build();
    result = search.execute(request);
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
    })
  @Test(groups = "search")
  public void caseChangeSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final CaseChangeEntryHandler srh = new CaseChangeEntryHandler();
    final String expected = TestUtils.readFileIntoString(ldifFile);

    // test no case change
    final SearchResponse noChangeResult = TestUtils.convertLdifToResult(expected);
    SearchResponse result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    TestUtils.assertEquals(noChangeResult, result);

    // test lower case attribute values
    srh.setAttributeNameCaseChange(CaseChange.NONE);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse lcValuesChangeResult = TestUtils.convertLdifToResult(expected);
    for (LdapAttribute la : lcValuesChangeResult.getEntry().getAttributes()) {
      final Set<String> s = la.getStringValues().stream().map(String::toLowerCase).collect(Collectors.toSet());
      la.clear();
      la.addStringValues(s);
    }
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    TestUtils.assertEquals(lcValuesChangeResult, result);

    // test upper case attribute names
    srh.setAttributeNameCaseChange(CaseChange.UPPER);
    srh.setAttributeValueCaseChange(CaseChange.NONE);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse ucNamesChangeResult = TestUtils.convertLdifToResult(expected);
    for (LdapAttribute la : ucNamesChangeResult.getEntry().getAttributes()) {
      la.setName(la.getName().toUpperCase());
    }
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    TestUtils.assertEquals(ucNamesChangeResult, result);

    // test lower case everything
    srh.setAttributeNameCaseChange(CaseChange.LOWER);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.LOWER);

    final SearchResponse lcAllChangeResult = TestUtils.convertLdifToResult(expected);
    for (LdapAttribute la : ucNamesChangeResult.getEntry().getAttributes()) {
      lcAllChangeResult.getEntry().setDn(lcAllChangeResult.getEntry().getDn().toLowerCase());
      la.setName(la.getName().toLowerCase());

      final Set<String> s = la.getStringValues().stream().map(String::toLowerCase).collect(Collectors.toSet());
      la.clear();
      la.addStringValues(s);
    }
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    TestUtils.assertEquals(ucNamesChangeResult, result);

    // test lower case specific attributes
    srh.setAttributeNames("givenName");
    srh.setAttributeNameCaseChange(CaseChange.NONE);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse lcgivenNameChangeResult = TestUtils.convertLdifToResult(expected);
    lcgivenNameChangeResult.getEntry().getAttributes().stream().filter(
      la -> la.getName().equals("givenName")).forEach(la -> {
        final Set<String> s = la.getStringValues().stream().map(String::toLowerCase).collect(Collectors.toSet());
        la.clear();
        la.addStringValues(s);
      });
    result = search.execute(
      dn,
      new SearchFilter(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    TestUtils.assertEquals(lcgivenNameChangeResult, result);
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
    })
  @Test(groups = "search")
  public void rangeHandlerSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    // OpenDJ will not parse DNs used by this entry handler
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    search.setSearchResultHandlers(new RangeEntryHandler());
    final SearchResponse result = search.execute(
      dn,
      new SearchFilter(filter),
      returnAttrs.split("\\|"),
      new ObjectSidHandler(), new ObjectGuidHandler());

    if (ResultCode.DECODING_ERROR == result.getResultCode()) {
      // ignore this test if not supported by the server
      throw new UnsupportedOperationException("LDAP server does not support this DN syntax");
    }
    // ignore the case of member; some directories return it in mixed case
    AssertJUnit.assertEquals(
      0,
      (new LdapEntryIgnoreCaseComparator("member")).compare(
        TestUtils.convertLdifToResult(expected).getEntry(),
        result.getEntry()));
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
    })
  @Test(groups = "search")
  public void getStatsSearch(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(new SearchFilter(filter))
      .controls(new GetStatsControl()).build();
    sr.setControls(new GetStatsControl());

    final SearchResponse response = search.execute(sr);
    final GetStatsControl ctrl = (GetStatsControl) response.getControl(GetStatsControl.OID);
    AssertJUnit.assertTrue(ctrl.getStatistics().size() > 1);

    final LdapAttribute whenCreated = response.getEntry().getAttribute("whenCreated");
    AssertJUnit.assertNotNull(whenCreated.getValue(new GeneralizedTimeValueTranscoder().decoder()));

    final LdapAttribute whenChanged = response.getEntry().getAttribute("whenChanged");
    AssertJUnit.assertNotNull(whenChanged.getValue(new GeneralizedTimeValueTranscoder().decoder()));
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
    })
  @Test(groups = "search")
  public void miscADControlsSearch(final String host, final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    try  {
      final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
      search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
      final SearchRequest sr = SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter))
        .controls(
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
          new ShowRecycledControl()).build();
      search.execute(sr);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      AssertJUnit.assertNotNull(e);
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
    })
  @Test(groups = "search")
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
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResponse specialCharsResult = TestUtils.convertLdifToResult(expected);

    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(filter, filterParameters.split("\\|")))
        .attributes(returnAttrs.split("\\|")).build());
    TestUtils.assertEquals(specialCharsResult, result);

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new SearchFilter(binaryFilter, new Object[] {LdapUtils.base64Decode(binaryFilterParameters)}))
        .attributes(returnAttrs.split("\\|")).build());
    TestUtils.assertEquals(specialCharsResult, result);
  }


  /**
   * @param  dn  to search on.
   * @param  ldifFile  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "quotedBaseDn",
      "quotedBaseDnSearchResults"
    })
  @Test(groups = "search")
  public void quoteInBaseDn(final String dn, final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResponse quotedResult = TestUtils.convertLdifToResult(expected);

    final SearchResponse result = search.execute(SearchRequest.objectScopeSearchRequest(dn));
    TestUtils.assertEquals(quotedResult, result);
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
    })
  @Test(groups = "search")
  public void rewriteSearch(final String dn, final String filter, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResponse specialCharsResult = TestUtils.convertLdifToResult(expected);
    specialCharsResult.getEntry().setDn(specialCharsResult.getEntry().getDn().replaceAll("\\\\", ""));

    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());

    // test special character searching
    final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
    final SearchResponse result = search.execute(request);
    if (ResultCode.NO_SUCH_OBJECT == result.getResultCode()) {
      // ignore this test if not supported by the server
      return;
    }
    TestUtils.assertEquals(specialCharsResult, result);
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
    })
  @Test(groups = "search")
  public void searchExceeded(final String dn, final String filter, final int resultsSize)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(dn);
    request.setSizeLimit(resultsSize);

    request.setFilter(new SearchFilter("(uugid=*)"));

    SearchResponse response = search.execute(request);
    AssertJUnit.assertEquals(resultsSize, response.entrySize());
    AssertJUnit.assertEquals(ResultCode.SIZE_LIMIT_EXCEEDED, response.getResultCode());

    request.setFilter(new SearchFilter(filter));
    response = search.execute(request);
    AssertJUnit.assertEquals(resultsSize, response.entrySize());
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
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
    })
  @Test(groups = "search")
  public void searchReferral(final String dn, final String filter)
    throws Exception
  {
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    // expects a referral on the dn ou=referrals
    final String referralDn = "ou=referrals," + DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(new SearchFilter(filter));

    final ConnectionConfig cc = TestUtils.readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final ConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    SearchResponse response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertEquals(1, response.getReferralURLs().length);
    AssertJUnit.assertEquals("ldap://localhost:389/ou=people,dc=vt,dc=edu??one", response.getReferralURLs()[0]);

    search.setSearchResultHandlers(new FollowSearchReferralHandler(url -> {
      final ConnectionConfig refConfig = ConnectionConfig.copy(cc);
      refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cc.getLdapUrl()).getHostname()));
      return new DefaultConnectionFactory(refConfig);
    }));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() > 0);
    AssertJUnit.assertTrue(response.getReferralURLs().length == 0);

    // chase referrals

    // default limit
    final String chaseReferralDn = "cn=0,ou=referrals-chase," + DnParser.substring(dn, 1);
    request.setBaseDn(chaseReferralDn);
    request.setSearchScope(SearchScope.SUBTREE);
    request.setFilter(new SearchFilter("uupid=dhawes"));
    search.setSearchResultHandlers(new FollowSearchReferralHandler());
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() > 0);

    // limit 0
    search.setSearchResultHandlers(new FollowSearchReferralHandler(0));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertEquals("ldap://ldap-test:10389/cn=1,ou=1,ou=referrals-chase,dc=vt,dc=edu??sub",
      response.getReferralURLs()[0]);

    // limit 1
    search.setSearchResultHandlers(new FollowSearchReferralHandler(1));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertEquals("ldap://ldap-test:10389/cn=2,ou=2,ou=referrals-chase,dc=vt,dc=edu??sub",
      response.getReferralURLs()[0]);

    // limit 2
    search.setSearchResultHandlers(new FollowSearchReferralHandler(2));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertEquals("ldap://ldap-test:10389/cn=3,ou=3,ou=referrals-chase,dc=vt,dc=edu??sub",
      response.getReferralURLs()[0]);

    // limit 3
    search.setSearchResultHandlers(new FollowSearchReferralHandler(3));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertEquals("ldap://ldap-test:10389/ou=people,dc=vt,dc=edu??sub", response.getReferralURLs()[0]);

    // limit 4
    search.setSearchResultHandlers(new FollowSearchReferralHandler(4));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() > 0);
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
    })
  @Test(groups = "search")
  public void searchReference(final String dn, final String filter)
    throws Exception
  {
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final List<String> refs = new ArrayList<>();

    // expects a referral on the root dn
    final String referralDn = DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(new SearchFilter(filter));

    final ConnectionConfig cc = TestUtils.readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final ConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    search.setReferenceHandlers((SearchReferenceHandler) uris -> refs.addAll(Arrays.asList(uris)));
    SearchResponse response = search.execute(request);
    AssertJUnit.assertTrue(response.entrySize() > 0);
    AssertJUnit.assertTrue(refs.size() > 0);
    for (String r : refs) {
      AssertJUnit.assertNotNull(r);
    }
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());

    refs.clear();
    final FollowSearchResultReferenceHandler srh = new FollowSearchResultReferenceHandler(
      3,
      url -> {
        final ConnectionConfig refConfig = ConnectionConfig.copy(cc);
        refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cc.getLdapUrl()).getHostname()));
        return new DefaultConnectionFactory(refConfig);
      });
    search.setSearchResultHandlers(srh);
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.entrySize() > 0);
    AssertJUnit.assertTrue(response.referenceSize() == 0);

    // chase search references

    // default limit
    final String referenceDn = "ou=references-chase," + DnParser.substring(dn, 1);
    request.setBaseDn(referenceDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setFilter(new SearchFilter("uupid=dhawes"));
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler());
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() > 0);

    // limit 0
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(0));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);
    AssertJUnit.assertFalse(response.getReferences().isEmpty());
    for (SearchResultReference s : response.getReferences()) {
      AssertJUnit.assertEquals("ldap://ldap-test:10389/ou=1,ou=references-chase,dc=vt,dc=edu??sub", s.getUris()[0]);
    }

    // limit 1
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(1));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);

    // limit 2
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(2));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);

    // limit 3
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(3));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() == 0);

    // limit 4
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(4));
    response = search.execute(request);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
    AssertJUnit.assertTrue(response.getEntries().size() > 0);
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
    })
  @Test(groups = "search")
  public void searchActiveDirectory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
      return;
    }

    final List<String> refs = new ArrayList<>();

    // expects a referral on the root dn
    final String referralDn = DnParser.substring(dn, 1);
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(new SearchFilter(filter));

    final ConnectionConfig cc = TestUtils.readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final ConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    search.setSearchResultHandlers(new PrimaryGroupIdHandler());
    search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
    search.setReferenceHandlers((SearchReferenceHandler) uris -> refs.addAll(Arrays.asList(uris)));
    SearchResponse response = search.execute(request);
    AssertJUnit.assertTrue(response.entrySize() > 0);
    AssertJUnit.assertTrue(refs.size() > 0);
    for (String r : refs) {
      AssertJUnit.assertNotNull(r);
    }
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());

    refs.clear();
    search.setSearchResultHandlers(new FollowSearchReferralHandler(new DefaultReferralConnectionFactory(cc)));
    response = search.execute(request);
    AssertJUnit.assertTrue(response.entrySize() > 0);
    AssertJUnit.assertEquals(ResultCode.SUCCESS, response.getResultCode());
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
    })
  @Test(
    groups = "search", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void getAttributes(final String dn, final String returnAttrs, final String results)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|")));
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
    })
  @Test(groups = "search")
  public void getAttributesBase64(final String dn, final String returnAttrs, final String results)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|"));
    request.setBinaryAttributes("jpegPhoto");

    final SearchResponse result = search.execute(request);
    AssertJUnit.assertEquals(
      TestUtils.convertStringToEntry(dn, results).getAttribute("jpegPhoto").getStringValue(),
      result.getEntry().getAttribute("jpegPhoto").getStringValue());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "search")
  public void getSaslMechanisms()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest("", new String[] {"supportedSASLMechanisms"}));
    AssertJUnit.assertTrue(result.getEntry().getAttributes().size() > 0);
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "search")
  public void getSupportedControls()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(TestUtils.createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest("", new String[] {"supportedcontrol"}));
    AssertJUnit.assertTrue(result.getEntry().getAttributes().size() > 0);
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
    })
  @Test(groups = "search")
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
    final SearchOperation search = new SearchOperation(TestUtils.createDigestMd5ConnectionFactory());
    final SearchResponse result = search.execute(
      new SearchRequest(
        dn,
        new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
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
    })
  @Test(groups = "search")
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
    try {
      final SearchOperation search = new SearchOperation(TestUtils.createCramMd5ConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      if (result.getResultCode() == ResultCode.AUTH_METHOD_NOT_SUPPORTED) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support CRAM-MD5");
      }
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      AssertJUnit.assertNotNull(e);
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
    })
  @Test(groups = "search")
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
    try {
      final SearchOperation search = new SearchOperation(TestUtils.createSaslExternalConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
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
    })
  @Test(groups = "search", enabled = false)
  // TODO figure out hostname resolution in docker compose
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
    System.setProperty("sun.security.krb5.debug", "true");
    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    System.setProperty("java.security.krb5.realm", krb5Realm);
    System.setProperty("java.security.krb5.kdc", ldapUrl.getHostname());

    final String expected = TestUtils.readFileIntoString(ldifFile);

    try {
      final SearchOperation search = new SearchOperation(TestUtils.createGssApiConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new SearchFilter(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      AssertJUnit.assertNotNull(e);
    } finally {
      System.clearProperty("sun.security.krb5.debug");
      System.clearProperty("java.security.auth.login.config");
      System.clearProperty("javax.security.auth.useSubjectCredsOnly");
      System.clearProperty("java.security.krb5.realm");
      System.clearProperty("java.security.krb5.kdc");
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
    })
  @Test(groups = "search")
  public void pooledSearch(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(dn);
    request.setFilter(new SearchFilter(filter, filterParameters.split("\\|")));
    request.setReturnAttributes(returnAttrs.split("\\|"));

    final String expected = TestUtils.readFileIntoString(ldifFile);

    final ConnectionFactory cf = new DefaultConnectionFactory(TestUtils.readConnectionConfig(null));
    final SearchOperation search = new SearchOperation();
    SearchResponse result = search.execute(cf, request);
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    PooledConnectionFactory pcf = new PooledConnectionFactory(TestUtils.readConnectionConfig(null));
    pcf.setConnectOnCreate(false);
    pcf.initialize();
    result = search.execute(pcf, request);
    pcf.close();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);

    pcf = new PooledConnectionFactory(TestUtils.readConnectionConfig(null));
    pcf.setConnectOnCreate(true);
    pcf.initialize();
    result = search.execute(pcf, request);
    pcf.close();
    TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), result);
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
    })
  @Test(groups = "search")
  public void searchWorker(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final String expected = TestUtils.readFileIntoString(ldifFile);

    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final SearchOperationWorker op = new SearchOperationWorker();
    try {
      op.getOperation().setConnectionFactory(cf);
      op.getOperation().setRequest(SearchRequest.builder().dn(dn).build());
      final Collection<SearchResponse> results = op.execute(
        new SearchFilter[] {new SearchFilter(filter, filterParameters.split("\\|"))},
        returnAttrs.split("\\|"));
      AssertJUnit.assertEquals(1, results.size());
      TestUtils.assertEquals(TestUtils.convertLdifToResult(expected), results.iterator().next());
    } finally {
      cf.close();
    }
  }
}
