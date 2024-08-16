/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import java.util.ArrayList;
import java.util.List;
import org.ldaptive.AbstractTest;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapEntry;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.concurrent.SearchOperationWorker;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link SearchTemplatesOperation}.
 *
 * @author  Middleware Services
 */
public class SearchTemplatesOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry[] testLdapEntries;

  /** Executor to test. */
  private SearchTemplatesOperation searchOperation;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry26")
  @BeforeClass(groups = "templates")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntries = new LdapEntry[5];
    for (int i = 0; i < 5; i++) {
      testLdapEntries[i] = convertLdifToResult(ldif).getEntry();
      final String cn = testLdapEntries[i].getAttribute("cn").getStringValue();
      testLdapEntries[i].setDn(testLdapEntries[i].getDn().replaceFirst(cn, cn + i));
      testLdapEntries[i].getAttribute("cn").addStringValues(cn + i);
      super.createLdapEntry(testLdapEntries[i]);
      // remove objectClass as active directory adds some additional ones
      testLdapEntries[i].removeAttribute("objectClass");
    }
  }


  /**
   * @param  baseDn  to search on
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("ldapBaseDn")
  @BeforeClass(groups = "templates")
  public void createExecutor(final String baseDn)
    throws Exception
  {
    final ConnectionConfig cc = readConnectionConfig(null);
    final PooledConnectionFactory cf = new PooledConnectionFactory(cc);
    cf.setMinPoolSize(5);
    cf.setMaxPoolSize(10);
    cf.initialize();

    searchOperation = new SearchTemplatesOperation(
      new SearchOperationWorker(new SearchOperation(cf, SearchRequest.builder().dn(baseDn).build())),
      new SearchTemplates(
        "(|(givenName={term1})(sn={term1}))",
        "(|(givenName={term1}*)(sn={term1}*))",
        "(|(givenName=*{term1}*)(sn=*{term1}*))",
        "(|(departmentNumber={term1})(mail={term1}))",
        "(|(departmentNumber={term1})(mail={term1}*))",
        "(|(departmentNumber={term1})(mail=*{term1}*))"),
      new SearchTemplates(
        "(&(givenName={term1})(sn={term2}))",
        "(department={term1} {term2})",
        "(department={term1}* {term2}*)",
        "(department=*{term1}* *{term2}*)"),
      new SearchTemplates(
        "(|(&(givenName={term1})(sn={term3}))" +
        "(&(givenName={term2})(sn={term3})))",
        "(|(cn={term1} {term2} {term3})(cn={term2} {term1} {term3}))",
        "(|(&(givenName={term1})(sn={term3}))" +
        "(&(givenName={term2})(sn={term3})))"));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "templates")
  public void deleteLdapEntry()
    throws Exception
  {
    for (int i = 0; i < testLdapEntries.length; i++) {
      super.deleteLdapEntry(testLdapEntries[i].getDn());
    }
    searchOperation.close();
  }


  /**
   * Sample executor data.
   *
   * @return  executor data
   */
  @DataProvider(name = "search-data")
  public Object[][] createTestData()
  {
    return new Object[][] {
      {"troosevelt", },
      {"0846", },
      {"theodore roosevelt", },
    };
  }


  /**
   * @param  query  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "templates", dataProvider = "search-data", threadPoolSize = 3, invocationCount = 50,
    timeOut = 60000)
  public void search(final String query)
    throws Exception
  {
    final Query q = new Query(query);
    q.setReturnAttributes(testLdapEntries[0].getAttributeNames());

    SearchResponse sr = searchOperation.execute(q);
    sr = SearchResponse.sort(sr);
    assertThat(sr).isNotNull().extracting(SearchResponse::entrySize).isEqualTo(5);
    final List<LdapEntry> l = new ArrayList<>(sr.getEntries());
    LdapEntryAssert.assertThat(l.get(0)).isSame(testLdapEntries[0]);
    LdapEntryAssert.assertThat(l.get(1)).isSame(testLdapEntries[1]);
    LdapEntryAssert.assertThat(l.get(2)).isSame(testLdapEntries[2]);
    LdapEntryAssert.assertThat(l.get(3)).isSame(testLdapEntries[3]);
    LdapEntryAssert.assertThat(l.get(4)).isSame(testLdapEntries[4]);
  }


  @Test(groups = "templates")
  public void searchFrom()
  {
    final Query q = new Query("0846");
    q.setFromResult(2);
    q.setReturnAttributes(testLdapEntries[0].getAttributeNames());

    SearchResponse sr = searchOperation.execute(q);
    sr = SearchResponse.sort(sr);
    assertThat(sr).isNotNull().extracting(SearchResponse::entrySize).isEqualTo(3);
    final List<LdapEntry> l = new ArrayList<>(sr.getEntries());
    LdapEntryAssert.assertThat(l.get(0)).isSame(testLdapEntries[2]);
    LdapEntryAssert.assertThat(l.get(1)).isSame(testLdapEntries[3]);
    LdapEntryAssert.assertThat(l.get(2)).isSame(testLdapEntries[4]);
  }


  @Test(groups = "templates")
  public void searchTo()
  {
    final Query q = new Query("0846");
    q.setToResult(3);
    q.setReturnAttributes(testLdapEntries[0].getAttributeNames());

    SearchResponse sr = searchOperation.execute(q);
    sr = SearchResponse.sort(sr);
    assertThat(sr).isNotNull().extracting(SearchResponse::entrySize).isEqualTo(3);
    final List<LdapEntry> l = new ArrayList<>(sr.getEntries());
    LdapEntryAssert.assertThat(l.get(0)).isSame(testLdapEntries[0]);
    LdapEntryAssert.assertThat(l.get(1)).isSame(testLdapEntries[1]);
    LdapEntryAssert.assertThat(l.get(2)).isSame(testLdapEntries[2]);
  }


  @Test(groups = "templates")
  public void searchFromTo()
  {
    final Query q = new Query("0846");
    q.setFromResult(2);
    q.setToResult(4);
    q.setReturnAttributes(testLdapEntries[0].getAttributeNames());

    SearchResponse sr = searchOperation.execute(q);
    sr = SearchResponse.sort(sr);
    assertThat(sr).isNotNull().extracting(SearchResponse::entrySize).isEqualTo(2);
    final List<LdapEntry> l = new ArrayList<>(sr.getEntries());
    LdapEntryAssert.assertThat(l.get(0)).isSame(testLdapEntries[2]);
    LdapEntryAssert.assertThat(l.get(1)).isSame(testLdapEntries[3]);
  }
}
