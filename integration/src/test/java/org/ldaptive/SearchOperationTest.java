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
import org.ldaptive.control.MatchedValuesRequestControl;
import org.ldaptive.control.PagedResultsControl;
import org.ldaptive.control.ProxyAuthorizationControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.control.SortRequestControl;
import org.ldaptive.control.SortResponseControl;
import org.ldaptive.control.VirtualListViewRequestControl;
import org.ldaptive.control.VirtualListViewResponseControl;
import org.ldaptive.dn.Dn;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.CaseChangeEntryHandler.CaseChange;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.MergeAttributeEntryHandler;
import org.ldaptive.handler.MergeResultHandler;
import org.ldaptive.handler.NoOpEntryHandler;
import org.ldaptive.handler.RecursiveResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.referral.DefaultReferralConnectionFactory;
import org.ldaptive.referral.FollowSearchReferralHandler;
import org.ldaptive.referral.FollowSearchResultReferenceHandler;
import org.ldaptive.referral.PooledReferralConnectionFactory;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

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

  /** Entry created for ldap tests. */
  private static LdapEntry specialCharsLdapEntry4;

  /** Entries for group tests. */
  private static final Map<String, LdapEntry[]> GROUP_ENTRIES = new HashMap<>();

  static {
    // Initialize the map of group entries
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
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntry = convertLdifToEntry(ldif);
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
  @Parameters({
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
    GROUP_ENTRIES.get("2")[0] = convertLdifToEntry(readFileIntoString(ldifFile2));
    GROUP_ENTRIES.get("3")[0] = convertLdifToEntry(readFileIntoString(ldifFile3));
    GROUP_ENTRIES.get("4")[0] = convertLdifToEntry(readFileIntoString(ldifFile4));
    GROUP_ENTRIES.get("5")[0] = convertLdifToEntry(readFileIntoString(ldifFile5));
    // CheckStyle:Indentation ON

    for (Map.Entry<String, LdapEntry[]> e : GROUP_ENTRIES.entrySet()) {
      super.createLdapEntry(e.getValue()[0]);
    }

    final String baseDn = new Dn(GROUP_ENTRIES.get("2")[0].getDn()).subDn(1).format();
    // setup group relationships
    final ModifyOperation modify = new ModifyOperation(createSetupConnectionFactory());
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
    final String ldif = readFileIntoString(ldifFile);
    specialCharsLdapEntry = convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry);
  }


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createSpecialCharsEntry4")
  @BeforeClass(groups = "search")
  public void createSpecialCharsEntry4(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    specialCharsLdapEntry4 = convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(specialCharsLdapEntry4);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "search")
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry.getDn());
    super.deleteLdapEntry(specialCharsLdapEntry4.getDn());
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
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    final SearchResponse entryDnResult = convertLdifToResult(expected);
    entryDnResult.getEntry().addAttributes(new LdapAttribute("entryDN", entryDnResult.getEntry().getDn()));

    // test searching
    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
         .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    // test searching no attributes
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
        .returnAttributes(ReturnAttributes.NONE.value()).build());
    assertThat(result.getEntry().getAttributes().isEmpty()).isTrue();

    // test searching without handler
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new LdapEntryHandler[0]);
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    // test searching with multiple handlers
    final DnAttributeEntryHandler srh = new DnAttributeEntryHandler();
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new NoOpEntryHandler(), srh);
    // ignore the case of entryDN; some directories return those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(entryDnResult.getEntry(), "entryDN");

    // test that entry dn handler is no-op if attribute name conflicts
    srh.setDnAttributeName("givenName");
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      new NoOpEntryHandler(), srh);
    // ignore the case of entryDN; some directories return those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "entryDN");
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "searchDn",
    "searchReturnAttrs",
    "searchResults"
  })
  @Test(groups = "search")
  public void searchFilters(
    final String dn,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    // presence filter
    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(objectClass=*)(uid=2))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(objectClass=*)(uid=*))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    assertThat(result.entrySize()).isGreaterThan(10);

    // extensible filter
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(uid:caseExactMatch:=2)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    // substring filter
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(cn=*hn Adams)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(cn=* *)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    assertThat(result.entrySize()).isGreaterThan(10);

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(cn=John Adam*)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(mail=jad*@*org)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(cn=*ohn A*m*)")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    // greater than filter
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(uid=2)(createTimestamp>=20000101000000Z))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(uid=*)(createTimestamp>=20000101000000Z))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    assertThat(result.entrySize()).isGreaterThan(10);

    // less than filter
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(uid=2)(createTimestamp<=21000101000000Z))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(uid=*)(createTimestamp<=21000101000000Z))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    assertThat(result.entrySize()).isGreaterThan(10);

    // approximate filter
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter("(&(givenName~=Jon)(mail=adams@ldaptive.org))")
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
  }


  /**
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchResponse expectedResult = convertLdifToResult(readFileIntoString(ldifFile));
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final SearchRequest subtreeRequest = SearchRequest.builder()
      .dn(new Dn(expectedResult.getEntry().getDn()).subDn(2).format())
      .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
      .returnAttributes(returnAttrs.split("\\|"))
      .scope(SearchScope.SUBTREE).build();
    SearchResponse result = search.execute(subtreeRequest);
    SearchResponseAssert.assertThat(result).isSame(expectedResult);

    final SearchRequest onelevelRequest = SearchRequest.builder()
      .dn(new Dn(expectedResult.getEntry().getDn()).subDn(1).format())
      .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
      .returnAttributes(returnAttrs.split("\\|"))
      .scope(SearchScope.ONELEVEL).build();
    result = search.execute(onelevelRequest);
    SearchResponseAssert.assertThat(result).isSame(expectedResult);

    final SearchRequest objectRequest = SearchRequest.builder()
      .dn(expectedResult.getEntry().getDn())
      .filter("(objectClass=*)")
      .returnAttributes(returnAttrs.split("\\|"))
      .scope(SearchScope.OBJECT).build();
    result = search.execute(objectRequest);
    SearchResponseAssert.assertThat(result).isSame(expectedResult);

    final SearchRequest subordinateRequest = SearchRequest.builder()
      .dn(new Dn(expectedResult.getEntry().getDn()).subDn(2).format())
      .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
      .returnAttributes(returnAttrs.split("\\|"))
      .scope(SearchScope.SUBORDINATE).build();
    result = search.execute(subordinateRequest);
    SearchResponseAssert.assertThat(result).isSame(expectedResult);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    // test searching, no attributes
    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
        .returnAttributes(ReturnAttributes.NONE.value()).build());
    assertThat(result.getEntry()).isNotNull();
    assertThat(result.getEntry().getAttributes().isEmpty()).isTrue();

    // test searching, user attributes
    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
        .returnAttributes(ReturnAttributes.ALL_USER.value()).build());
    assertThat(result.getEntry()).isNotNull();
    assertThat(result.getEntry().getAttribute("cn")).isNotNull();
    assertThat(result.getEntry().getAttribute("createTimestamp")).isNull();

    // test searching, operations attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
          .returnAttributes(ReturnAttributes.ALL_OPERATIONAL.add("createTimestamp")).build());
    } else {
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
          .returnAttributes(ReturnAttributes.ALL_OPERATIONAL.value()).build());
    }
    assertThat(result.getEntry()).isNotNull();
    assertThat(result.getEntry().getAttribute("cn")).isNull();
    assertThat(result.getEntry().getAttribute("createTimestamp")).isNotNull();

    // test searching, all attributes
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      // directory ignores '+'
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
          .returnAttributes(ReturnAttributes.ALL.add("createTimestamp")).build());
    } else {
      result = search.execute(
        SearchRequest.builder()
          .dn(dn)
          .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
          .returnAttributes(ReturnAttributes.ALL.value()).build());
    }
    assertThat(result.getEntry()).isNotNull();
    assertThat(result.getEntry().getAttribute("cn")).isNotNull();
    assertThat(result.getEntry().getAttribute("createTimestamp")).isNotNull();
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "pagedSearchDn",
    "pagedSearchFilter",
    "pagedSearchReturnAttrs",
    "pagedSearchResults"
  })
  @Test(groups = "search")
  public void pagedSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final PagedResultsControl prc = new PagedResultsControl(1, true);
      final SearchOperation search = new SearchOperation(cf);
      final String expected = readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .returnAttributes(returnAttrs.split("\\|"))
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
      SearchResponseAssert.assertThat(pagedResults).isSame(convertLdifToResult(expected), "member", "contactPerson");
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
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
      final SearchOperation search = new SearchOperation(createConnectionFactory());
      final String expected = readFileIntoString(ldifFile);

      // test searching
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .returnAttributes(returnAttrs.split("\\|"))
        .controls(src, vlvrc).build();

      SearchResponse result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected), "member", "contactPerson");
      final SortResponseControl srResCtl =
        (SortResponseControl) result.getControl(SortResponseControl.OID);
      assertThat(srResCtl).isNotNull();
      assertThat(srResCtl.getSortResult()).isEqualTo(ResultCode.SUCCESS);

      final VirtualListViewResponseControl vlvResCtl =
        (VirtualListViewResponseControl) result.getControl(VirtualListViewResponseControl.OID);
      assertThat(vlvResCtl).isNotNull();
      assertThat(vlvResCtl.getViewResult()).isEqualTo(ResultCode.SUCCESS);
      assertThat(vlvResCtl.getTargetPosition()).isEqualTo(3);
      assertThat(vlvResCtl.getContentCount()).isEqualTo(4);
      assertThat(vlvResCtl.getContextID()).isNotNull();
      contextID = vlvResCtl.getContextID();

      vlvrc = new VirtualListViewRequestControl("group4", 1, 1, contextID, true);
      request.setControls(src, vlvrc);
      result = search.execute(request);
      // ignore the case of member and contactPerson;
      // some directories return those in mixed case
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected), "member", "contactPerson");
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
      final SearchOperation search = new SearchOperation(createConnectionFactory());

      SortRequestControl src = new SortRequestControl(new SortKey[] {new SortKey("uugid", "caseExactMatch")}, true);

      // test sort by uugid
      final SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(src).build();
      SearchResponse result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }

      // confirm sorted
      int i = 2;
      for (LdapEntry e : result.getEntries()) {
        assertThat(e.getAttribute("uid").getStringValue()).isEqualTo(String.valueOf(2000 + i));
        i++;
      }

      // test sort by uid
      src = new SortRequestControl(new SortKey[] {new SortKey("uid", "integerMatch", true)}, true);
      request.setControls(src);
      result = search.execute(request);

      // confirm sorted
      i = 5;
      for (LdapEntry e : result.getEntries()) {
        assertThat(e.getAttribute("uid").getStringValue()).isEqualTo(String.valueOf(2000 + i));
        i--;
      }
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      assertThat(e).isNotNull();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "matchedValuesSearchDn",
    "matchedValuesSearchFilter"
  })
  @Test(groups = "search")
  public void matchValuesSearch(final String dn, final String filter)
    throws Exception
  {
    try {
      final SearchOperation search = new SearchOperation(createConnectionFactory());

      // test mail presence
      SearchRequest request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(new MatchedValuesRequestControl(
          new String[] {"(mail=*)"}, true))
        .build();
      SearchResponse result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(result.entrySize()).isEqualTo(1);
      assertThat(result.getEntry().size()).isEqualTo(1);
      LdapAttribute attr = result.getEntry().getAttribute();
      assertThat(attr.getName()).isEqualTo("mail");
      assertThat(attr.getStringValues().toArray(new String[0]))
        .containsExactlyInAnyOrder("jadams@ldaptive.org", "john.adams@ldaptive.org", "adams@ldaptive.org");

      // test mail equality
      request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(new MatchedValuesRequestControl(
          new String[] {"(mail=john.adams@ldaptive.org)"}, true))
        .build();
      result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(result.entrySize()).isEqualTo(1);
      assertThat(result.getEntry().size()).isEqualTo(1);
      attr = result.getEntry().getAttribute();
      assertThat(attr.getName()).isEqualTo("mail");
      assertThat(attr.size()).isEqualTo(1);
      assertThat(attr.getStringValue()).isEqualTo("john.adams@ldaptive.org");

      // test mail substring
      request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(new MatchedValuesRequestControl(
          new String[] {"(mail=j*adams*)"}, true))
        .build();
      result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(result.entrySize()).isEqualTo(1);
      assertThat(result.getEntry().size()).isEqualTo(1);
      attr = result.getEntry().getAttribute();
      assertThat(attr.getName()).isEqualTo("mail");
      assertThat(attr.getStringValues().toArray(new String[0]))
        .containsExactlyInAnyOrder("jadams@ldaptive.org", "john.adams@ldaptive.org");

      // test mail extensible
      request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(new MatchedValuesRequestControl(
          new String[] {"(mail:caseExactIA5Match:=john.adams@ldaptive.org)"},
          true))
        .build();
      result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(result.entrySize()).isEqualTo(1);
      assertThat(result.getEntry().size()).isEqualTo(1);
      attr = result.getEntry().getAttribute();
      assertThat(attr.getName()).isEqualTo("mail");
      assertThat(attr.size()).isEqualTo(1);
      assertThat(attr.getStringValue()).isEqualTo("john.adams@ldaptive.org");

      // test multiple filters
      request = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
        .controls(new MatchedValuesRequestControl("(mail=john.adams@ldaptive.org)", "(sn=Adams)"))
        .build();
      result = search.execute(request);
      if (result.getResultCode() == ResultCode.UNAVAILABLE_CRITICAL_EXTENSION) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(result.entrySize()).isEqualTo(1);
      assertThat(result.getEntry().size()).isEqualTo(2);
      assertThat(result.getEntry().getAttribute("mail").getStringValue()).isEqualTo("john.adams@ldaptive.org");
      assertThat(result.getEntry().getAttribute("sn").getStringValue()).isEqualTo("Adams");

    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      assertThat(e).isNotNull();
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
  @Parameters({
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
      final SearchOperation search = new SearchOperation(createConnectionFactory());

      final SearchRequest request = SearchRequest.builder().dn(dn).filter(filter).build();

      // no authz
      SearchResponse response = search.execute(request);
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(response.entrySize()).isEqualTo(1);

      // anonymous authz
      request.setControls(new ProxyAuthorizationControl("dn:"));
      response = search.execute(request);
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION == response.getResultCode()) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      }
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(response.entrySize()).isEqualTo(0);

      // authz denied
      request.setControls(new ProxyAuthorizationControl("dn:" + authzTo));
      response = search.execute(request);
      assertThat(response.getResultCode()).isEqualTo(ResultCode.AUTHORIZATION_DENIED);

      // add authzTo
      final ModifyOperation modify = new ModifyOperation(createConnectionFactory());
      modify.execute(
        new ModifyRequest(
          authzFrom,
          new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("authzTo", "dn:" + authzTo))));
      addedAttribute = true;

      response = search.execute(request);
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(response.entrySize()).isEqualTo(1);

    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      assertThat(e).isNotNull();
    } finally {
      if (addedAttribute) {
        try {
          // remove authzTo
          final ModifyOperation modify = new ModifyOperation(createConnectionFactory());
          modify.execute(
            new ModifyRequest(
              authzFrom,
              new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("authzTo"))));
        } catch (LdapException e) {
          fail(e.getMessage());
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
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveResultHandler rsrh = new RecursiveResultHandler("member", "uugid", "uid");
    search.setSearchResultHandlers(rsrh);

    final SearchResponse result = search.execute(
      dn, new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|"));

    // ignore the case of member and contactPerson; some directories return those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "member", "contactPerson");
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    // test recursive searching
    final RecursiveResultHandler rsrh = new RecursiveResultHandler("member", "member");
    search.setSearchResultHandlers(rsrh);

    final SearchResponse result = search.execute(dn, new FilterTemplate(filter), returnAttrs.split("\\|"));

    // ignore the case of member and contactPerson; some directories return those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "member");
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "mergeSearchDn",
    "mergeSearchFilter",
    "mergeSearchReturnAttrs",
    "mergeSearchResults"
  })
  @Test(groups = "search")
  public void mergeSearch(final String dn, final String filter, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    search.setSearchResultHandlers(new MergeResultHandler());

    final String expected = readFileIntoString(ldifFile);

    // test result merge
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .returnAttributes(returnAttrs.split("\\|")).build();

    final SearchResponse result = search.execute(sr);
    // ignore the case of member and contactPerson; some directories return
    // those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "member", "contactPerson");
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    search.setSearchResultHandlers(new MergeResultHandler());

    final String expected = readFileIntoString(ldifFile);

    // test result merge
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .returnAttributes(returnAttrs.split("\\|")).build();

    final SearchResponse result = search.execute(sr);
    // ignore the case of member and contactPerson; some directories return those in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "member", "contactPerson");
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    final String expected = readFileIntoString(ldifFile);

    // test merge searching
    final MergeAttributeEntryHandler handler = new MergeAttributeEntryHandler();
    handler.setMergeAttributeName("cn");
    handler.setAttributeNames("displayName", "givenName", "sn");

    final SearchResponse result = search.execute(
      dn,
      filter,
      returnAttrs.split("\\|"),
      handler);
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttr  to return from search.
   * @param  base64Value  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "binarySearchDn",
    "binarySearchFilter",
    "binarySearchReturnAttr",
    "binarySearchResult"
  })
  @Test(groups = "search")
  public void binarySearch(final String dn, final String filter, final String returnAttr, final String base64Value)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());

    // test binary searching
    SearchRequest request = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .returnAttributes(returnAttr)
      .binaryAttributes(returnAttr).build();

    SearchResponse result = search.execute(request);
    assertThat(result.getEntry().getAttribute().isBinary()).isTrue();
    assertThat(result.getEntry().getAttribute().getStringValue()).isEqualTo(base64Value);

    request = SearchRequest.builder().dn(dn).filter(filter).returnAttributes("sn").build();
    result = search.execute(request);
    assertThat(result.getEntry().getAttribute().isBinary()).isFalse();
    assertThat(result.getEntry().getAttribute().getBinaryValue()).isNotNull();

    request = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .returnAttributes("sn")
      .binaryAttributes("sn").build();
    result = search.execute(request);
    assertThat(result.getEntry().getAttribute().isBinary()).isTrue();
    assertThat(result.getEntry().getAttribute().getBinaryValue()).isNotNull();

    request = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .returnAttributes("userCertificate;binary").build();
    result = search.execute(request);
    assertThat(result.getEntry().getAttribute().isBinary()).isTrue();
    assertThat(result.getEntry().getAttribute().getBinaryValue()).isNotNull();
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
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final CaseChangeEntryHandler srh = new CaseChangeEntryHandler();
    final String expected = readFileIntoString(ldifFile);

    // test no case change
    final SearchResponse noChangeResult = convertLdifToResult(expected);
    SearchResponse result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    SearchResponseAssert.assertThat(result).isSame(noChangeResult);

    // test lower case attribute values
    srh.setAttributeNameCaseChange(CaseChange.NONE);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse lcValuesChangeResult = convertLdifToResult(expected);
    for (LdapAttribute la : lcValuesChangeResult.getEntry().getAttributes()) {
      final Set<String> s = la.getStringValues().stream().map(LdapUtils::toLowerCase).collect(Collectors.toSet());
      la.clear();
      la.addStringValues(s);
    }
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    SearchResponseAssert.assertThat(result).isSame(lcValuesChangeResult);

    // test upper case attribute names
    srh.setAttributeNameCaseChange(CaseChange.UPPER);
    srh.setAttributeValueCaseChange(CaseChange.NONE);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse ucNamesChangeResult = convertLdifToResult(expected);
    for (LdapAttribute la : ucNamesChangeResult.getEntry().getAttributes()) {
      la.setName(LdapUtils.toUpperCase(la.getName()));
    }
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    SearchResponseAssert.assertThat(result).isSame(ucNamesChangeResult);

    // test lower case everything
    srh.setAttributeNameCaseChange(CaseChange.LOWER);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.LOWER);

    final SearchResponse lcAllChangeResult = convertLdifToResult(expected);
    lcAllChangeResult.getEntry().setDn(LdapUtils.toLowerCase(lcAllChangeResult.getEntry().getDn()));
    for (LdapAttribute la : lcAllChangeResult.getEntry().getAttributes()) {
      la.setName(LdapUtils.toLowerCase(la.getName()));
      final Set<String> s = la.getStringValues().stream().map(LdapUtils::toLowerCase).collect(Collectors.toSet());
      la.clear();
      la.addStringValues(s);
    }
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    SearchResponseAssert.assertThat(result).isSame(lcAllChangeResult);

    // test lower case specific attributes
    srh.setAttributeNames("givenName");
    srh.setAttributeNameCaseChange(CaseChange.NONE);
    srh.setAttributeValueCaseChange(CaseChange.LOWER);
    srh.setDnCaseChange(CaseChange.NONE);

    final SearchResponse lcgivenNameChangeResult = convertLdifToResult(expected);
    lcgivenNameChangeResult.getEntry().getAttributes().stream().filter(
      la -> la.getName().equals("givenName")).forEach(la -> {
        final Set<String> s = la.getStringValues().stream().map(LdapUtils::toLowerCase).collect(Collectors.toSet());
        la.clear();
        la.addStringValues(s);
      });
    result = search.execute(
      dn,
      new FilterTemplate(filter, filterParameters.split("\\|")),
      returnAttrs.split("\\|"),
      srh);
    SearchResponseAssert.assertThat(result).isSame(lcgivenNameChangeResult);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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

    final String expected = readFileIntoString(ldifFile);

    final SearchOperation search = new SearchOperation(createConnectionFactory());
    search.setSearchResultHandlers(new RangeEntryHandler());
    final SearchResponse result = search.execute(
      dn,
      filter,
      returnAttrs.split("\\|"),
      new ObjectSidHandler(), new ObjectGuidHandler());

    if (ResultCode.DECODING_ERROR == result.getResultCode()) {
      // ignore this test if not supported by the server
      throw new UnsupportedOperationException("LDAP server does not support this DN syntax");
    }
    // ignore the case of member; some directories return it in mixed case
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertLdifToEntry(expected), "member");
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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

    final SearchOperation search = new SearchOperation(createConnectionFactory());
    search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
    final SearchRequest sr = SearchRequest.builder()
      .dn(dn)
      .filter(filter)
      .controls(new GetStatsControl()).build();
    sr.setControls(new GetStatsControl());

    final SearchResponse response = search.execute(sr);
    final GetStatsControl ctrl = (GetStatsControl) response.getControl(GetStatsControl.OID);
    assertThat(ctrl.getStatistics().size()).isGreaterThan(1);

    final LdapAttribute whenCreated = response.getEntry().getAttribute("whenCreated");
    assertThat(whenCreated.getValue(new GeneralizedTimeValueTranscoder().decoder())).isNotNull();

    final LdapAttribute whenChanged = response.getEntry().getAttribute("whenChanged");
    assertThat(whenChanged.getValue(new GeneralizedTimeValueTranscoder().decoder())).isNotNull();
  }


  /**
   * @param  host  for verify name
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
      final SearchOperation search = new SearchOperation(createConnectionFactory());
      search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
      final SearchRequest sr = SearchRequest.builder()
        .dn(dn)
        .filter(filter)
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
      assertThat(e).isNotNull();
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
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final String expected = readFileIntoString(ldifFile);
    final SearchResponse specialCharsResult = convertLdifToResult(expected);

    SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(specialCharsResult);

    result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(binaryFilter, new Object[] {LdapUtils.base64Decode(binaryFilterParameters)}))
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(specialCharsResult);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  filterParameters  to replace parameters in filter with.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "specialCharSearchDn4",
    "specialCharSearchFilter4",
    "specialCharSearchFilterParameters4",
    "specialCharReturnAttrs4",
    "specialCharSearchResults4"
  })
  @Test(groups = "search")
  public void specialCharsSearch4(
    final String dn,
    final String filter,
    final String filterParameters,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final String expected = readFileIntoString(ldifFile);
    final SearchResponse specialCharsResult = convertLdifToResult(expected);

    final SearchResponse result = search.execute(
      SearchRequest.builder()
        .dn(dn)
        .filter(new FilterTemplate(filter, filterParameters.split("\\|")))
        .returnAttributes(returnAttrs.split("\\|")).build());
    SearchResponseAssert.assertThat(result).isSame(specialCharsResult);
  }


  /**
   * @param  dn  to search on.
   * @param  ldifFile  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "quotedBaseDn",
    "quotedBaseDnSearchResults"
  })
  @Test(groups = "search")
  public void quoteInBaseDn(final String dn, final String ldifFile)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final String expected = readFileIntoString(ldifFile);
    final SearchResponse quotedResult = convertLdifToResult(expected);

    final SearchResponse result = search.execute(SearchRequest.objectScopeSearchRequest(dn));
    SearchResponseAssert.assertThat(result).isSame(quotedResult);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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

    final String expected = readFileIntoString(ldifFile);
    final SearchResponse specialCharsResult = convertLdifToResult(expected);
    specialCharsResult.getEntry().setDn(specialCharsResult.getEntry().getDn().replaceAll("\\\\", ""));

    final SearchOperation search = new SearchOperation(createConnectionFactory());

    // test special character searching
    final SearchRequest request = new SearchRequest(dn, filter);
    final SearchResponse result = search.execute(request);
    if (ResultCode.NO_SUCH_OBJECT == result.getResultCode()) {
      // ignore this test if not supported by the server
      return;
    }
    SearchResponseAssert.assertThat(result).isSame(specialCharsResult);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   * @param  resultsSize  of search results.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "searchExceededDn",
    "searchExceededFilter",
    "searchExceededResultsSize"
  })
  @Test(groups = "search")
  public void searchExceeded(final String dn, final String filter, final int resultsSize)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(dn);
    request.setSizeLimit(resultsSize);

    request.setFilter("(uugid=*)");

    SearchResponse response = search.execute(request);
    assertThat(response.entrySize()).isEqualTo(resultsSize);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SIZE_LIMIT_EXCEEDED);

    request.setFilter(filter);
    response = search.execute(request);
    assertThat(response.entrySize()).isEqualTo(resultsSize);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final String referralDn = Dn.builder().add("ou=referrals").add(new Dn(dn).subDn(1)).build().format();
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(filter);

    final ConnectionConfig cc = readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final ConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    SearchResponse response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.REFERRAL);
    assertThat(response.getEntries().isEmpty()).isTrue();
    assertThat(response.getReferralURLs().length).isEqualTo(1);
    assertThat(response.getReferralURLs()[0]).isEqualTo("ldap://localhost:389/ou=test,dc=vt,dc=edu??one");

    search.setReferralResultHandler(new FollowSearchReferralHandler(url -> {
      final ConnectionConfig refConfig = ConnectionConfig.copy(cc);
      refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cc.getLdapUrl()).getHostname()));
      return new DefaultConnectionFactory(refConfig);
    }));
    search.setThrowCondition(ResultPredicate.NOT_SUCCESS);
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();
    assertThat(response.getReferralURLs().length).isEqualTo(0);
    search.setReferralResultHandler(null);

    search.setSearchResultHandlers(new FollowSearchReferralHandler(url -> {
      final ConnectionConfig refConfig = ConnectionConfig.copy(cc);
      refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cc.getLdapUrl()).getHostname()));
      return new DefaultConnectionFactory(refConfig);
    }));
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();
    assertThat(response.getReferralURLs().length).isEqualTo(0);

    // chase referrals

    // default limit
    final String chaseReferralDn =
      Dn.builder().add("cn=0,ou=referrals-chase").add(new Dn(dn).subDn(1)).build().format();
    request.setBaseDn(chaseReferralDn);
    request.setSearchScope(SearchScope.SUBTREE);
    request.setFilter("uupid=dhawes");
    search.setSearchResultHandlers(new FollowSearchReferralHandler());
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();

    // limit 0-3
    for (int i = 0; i < 4; i++) {
      search.setSearchResultHandlers(new FollowSearchReferralHandler(i));
      try {
        search.execute(request);
        fail("Should have thrown exception");
      } catch (Exception e) {
        assertThat(e).isExactlyInstanceOf(LdapException.class);
        assertThat(((LdapException) e).getResultCode()).isEqualTo(ResultCode.REFERRAL_LIMIT_EXCEEDED);
      }
    }

    // limit 4
    search.setSearchResultHandlers(new FollowSearchReferralHandler(4));
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final String referralDn = new Dn(dn).subDn(1).format();
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(filter);

    final ConnectionConfig cc = readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final ConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    search.setReferenceHandlers(reference -> refs.addAll(Arrays.asList(reference.getUris())));
    SearchResponse response = search.execute(request);
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(refs.isEmpty()).isFalse();
    for (String r : refs) {
      assertThat(r).isNotNull();
    }
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);

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
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(response.referenceSize()).isEqualTo(2);

    // chase search references

    // default limit
    final String referenceDn = Dn.builder().add("ou=references-chase").add(new Dn(dn).subDn(1)).build().format();
    request.setBaseDn(referenceDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setFilter("uupid=dhawes");
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler());
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();
    assertThat(response.getReferences().isEmpty()).isTrue();

    // limit 0-3
    for (int i = 0; i < 4; i++) {
      search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(i));
      try {
        search.execute(request);
        fail("Should have thrown exception");
      } catch (Exception e) {
        assertThat(e).isExactlyInstanceOf(LdapException.class);
        assertThat(((LdapException) e).getResultCode()).isEqualTo(ResultCode.REFERRAL_LIMIT_EXCEEDED);
      }
    }

    // limit 4
    search.setSearchResultHandlers(new FollowSearchResultReferenceHandler(4));
    response = search.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.getEntries().isEmpty()).isFalse();
    assertThat(response.getReferences().isEmpty()).isTrue();
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final String referralDn = new Dn(dn).subDn(1).format();
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(referralDn);
    request.setSearchScope(SearchScope.ONELEVEL);
    request.setReturnAttributes(ReturnAttributes.NONE.value());
    request.setFilter(filter);

    final ConnectionConfig cc = readConnectionConfig(null);
    cc.setConnectTimeout(Duration.ofMillis(500));
    cc.setResponseTimeout(Duration.ofMillis(500));
    final DefaultConnectionFactory cf = DefaultConnectionFactory.builder()
      .config(cc)
      .build();
    final SearchOperation search = new SearchOperation(cf);
    search.setSearchResultHandlers(new PrimaryGroupIdHandler());
    search.setEntryHandlers(new ObjectSidHandler(), new ObjectGuidHandler());
    search.setReferenceHandlers(reference -> refs.addAll(Arrays.asList(reference.getUris())));
    SearchResponse response = search.execute(request);
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(refs.isEmpty()).isFalse();
    for (String r : refs) {
      assertThat(r).isNotNull();
    }
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);

    refs.clear();
    search.setSearchResultHandlers(
      new FollowSearchReferralHandler(new DefaultReferralConnectionFactory(cf)));
    response = search.execute(request);
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);

    final PooledReferralConnectionFactory prcf = new PooledReferralConnectionFactory(
      PooledConnectionFactory.builder().config(cf.getConnectionConfig()).build());
    refs.clear();
    search.setSearchResultHandlers(new FollowSearchReferralHandler(prcf));
    try {
      response = search.execute(request);
    } finally {
      prcf.close();
    }
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
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
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|")));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(convertStringToEntry(dn, results));
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  results  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "getAttributesBase64Dn",
    "getAttributesBase64ReturnAttrs",
    "getAttributesBase64Results"
  })
  @Test(groups = "search")
  public void getAttributesBase64(final String dn, final String returnAttrs, final String results)
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|"));
    request.setBinaryAttributes("jpegPhoto");

    final SearchResponse result = search.execute(request);
    assertThat(result.getEntry().getAttribute("jpegPhoto").getStringValue())
      .isEqualTo(convertStringToEntry(dn, results).getAttribute("jpegPhoto").getStringValue());
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "search")
  public void getSaslMechanisms()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest("", new String[] {"supportedSASLMechanisms"}));
    assertThat(result.getEntry().getAttributes().isEmpty()).isFalse();
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "search")
  public void getSupportedControls()
    throws Exception
  {
    final SearchOperation search = new SearchOperation(createConnectionFactory());
    final SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest("", new String[] {"supportedcontrol"}));
    assertThat(result.getEntry().getAttributes().isEmpty()).isFalse();
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
  @Parameters({
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

    ConnectionFactory cf = createDigestMd5ConnectionFactory();
    final ConnectionConfig cc = ConnectionConfig.copy(cf.getConnectionConfig());
    assertThat(cc.getConnectionInitializers().length).isEqualTo(1);
    final BindConnectionInitializer initializer =
      copyBindConnectionInitializer((BindConnectionInitializer) cc.getConnectionInitializers()[0]);
    initializer.setBindCredential(new Credential("wrong-password"));
    cc.setConnectionInitializers(initializer);
    cf = new DefaultConnectionFactory(cc);
    try (Connection conn = cf.getConnection()) {
      conn.open();
      fail("DIGEST-MD5 should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(ConnectException.class);
    }

    final String expected = readFileIntoString(ldifFile);
    final SearchOperation search = new SearchOperation(createDigestMd5ConnectionFactory());
    final SearchResponse result = search.execute(
      new SearchRequest(
        dn,
        new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
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
  @Parameters({
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

    ConnectionFactory cf = createCramMd5ConnectionFactory();
    final ConnectionConfig cc = ConnectionConfig.copy(cf.getConnectionConfig());
    assertThat(cc.getConnectionInitializers().length).isEqualTo(1);
    final BindConnectionInitializer initializer =
      copyBindConnectionInitializer((BindConnectionInitializer) cc.getConnectionInitializers()[0]);
    initializer.setBindCredential(new Credential("wrong-password"));
    cc.setConnectionInitializers(initializer);
    cf = new DefaultConnectionFactory(cc);
    try (Connection conn = cf.getConnection()) {
      conn.open();
      fail("CRAM-MD5 should have thrown exception");
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(ConnectException.class);
    }

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createCramMd5ConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      if (result.getResultCode() == ResultCode.AUTH_METHOD_NOT_SUPPORTED) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support CRAM-MD5");
      }
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported by the directory
      assertThat(e).isNotNull();
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
  @Parameters({
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

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createSaslExternalConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
    "gssApiSearchDn",
    "gssApiSearchFilter",
    "gssApiSearchFilterParameters",
    "gssApiSearchReturnAttrs",
    "gssApiSearchResults"
  })
  @Test(groups = "search")
  public void gssApiSearch(
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

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createGssApiConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
    "gssApiSearchDn",
    "gssApiSearchFilter",
    "gssApiSearchFilterParameters",
    "gssApiSearchReturnAttrs",
    "gssApiSearchResults"
  })
  @Test(groups = "search")
  public void gssApiSearchQopAuth(
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

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createGssApiQopAuthConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
    "gssApiSearchDn",
    "gssApiSearchFilter",
    "gssApiSearchFilterParameters",
    "gssApiSearchReturnAttrs",
    "gssApiSearchResults"
  })
  @Test(groups = "search")
  public void gssApiSearchQopAuthInt(
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

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createGssApiQopAuthIntConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
    "gssApiSearchDn",
    "gssApiSearchFilter",
    "gssApiSearchFilterParameters",
    "gssApiSearchReturnAttrs",
    "gssApiSearchResults"
  })
  @Test(groups = "search")
  public void gssApiSearchQopAuthIntLdaps(
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

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createGssApiQopAuthIntLdapsConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
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
  @Parameters({
    "gssApiSearchDn",
    "gssApiSearchFilter",
    "gssApiSearchFilterParameters",
    "gssApiSearchReturnAttrs",
    "gssApiSearchResults"
  })
  @Test(groups = "search")
  public void gssApiSearchUsingConfig(
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

    System.setProperty("java.security.auth.login.config", "target/test-classes/ldap_jaas.config");
    System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

    final String expected = readFileIntoString(ldifFile);
    try {
      final SearchOperation search = new SearchOperation(createGssApiUseConfigConnectionFactory());
      final SearchResponse result = search.execute(
        new SearchRequest(
          dn,
          new FilterTemplate(filter, filterParameters.split("\\|")), returnAttrs.split("\\|")));
      SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
    } catch (UnsupportedOperationException e) {
      // ignore this test if not supported
      assertThat(e).isNotNull();
    } finally {
      System.clearProperty("java.security.auth.login.config");
      System.clearProperty("javax.security.auth.useSubjectCredsOnly");
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
  @Parameters({
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
    request.setFilter(new FilterTemplate(filter, filterParameters.split("\\|")));
    request.setReturnAttributes(returnAttrs.split("\\|"));

    final String expected = readFileIntoString(ldifFile);

    final ConnectionFactory cf = new DefaultConnectionFactory(readConnectionConfig(null));
    SearchResponse result = SearchOperation.execute(cf, request);
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    PooledConnectionFactory pcf = new PooledConnectionFactory(readConnectionConfig(null));
    pcf.setConnectOnCreate(false);
    pcf.initialize();
    result = SearchOperation.execute(pcf, request);
    pcf.close();
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));

    pcf = new PooledConnectionFactory(readConnectionConfig(null));
    pcf.setConnectOnCreate(true);
    pcf.initialize();
    result = SearchOperation.execute(pcf, request);
    pcf.close();
    SearchResponseAssert.assertThat(result).isSame(convertLdifToResult(expected));
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
  @Parameters({
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
    final String expected = readFileIntoString(ldifFile);

    final ConnectionFactory cf = createConnectionFactory();
    final SearchOperationWorker op = new SearchOperationWorker();
    op.getOperation().setConnectionFactory(cf);
    op.getOperation().setRequest(SearchRequest.builder().dn(dn).build());
    final Collection<SearchResponse> results = op.execute(
      new FilterTemplate[]{new FilterTemplate(filter, filterParameters.split("\\|"))},
      returnAttrs.split("\\|"));
    assertThat(results.size()).isEqualTo(1);
    SearchResponseAssert.assertThat(results.iterator().next()).isSame(convertLdifToResult(expected));
  }
}
