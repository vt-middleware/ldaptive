/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import org.ldaptive.AbstractTest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link MergeOperation}.
 *
 * @author  Middleware Services
 */
public class MergeOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry30")
  @BeforeClass(groups = "merge")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();

    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    Assert.assertFalse(super.entryExists(cf, testLdapEntry));

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry));
    Assert.assertTrue(super.entryExists(cf, testLdapEntry));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "merge")
  public void deleteLdapEntry()
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    Assert.assertTrue(super.entryExists(cf, testLdapEntry));

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry, true));
    Assert.assertFalse(super.entryExists(cf, testLdapEntry));
    merge.execute(new MergeRequest(testLdapEntry, true));
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "merge")
  public void merge()
    throws Exception
  {
    final LdapEntry source = new LdapEntry();
    source.setDn(testLdapEntry.getDn());

    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final MergeOperation merge = new MergeOperation(cf);
    final MergeRequest request = new MergeRequest(source);
    if (TestControl.isActiveDirectory()) {
      // remove objectClass for comparison testing related to AD
      testLdapEntry.removeAttribute("objectClass");
      source.addAttributes(testLdapEntry.getAttributes());
      // these attributes are single value in AD
      source.addAttributes(new LdapAttribute("givenName", "John"));
      source.addAttributes(new LdapAttribute("initials", "JC"));
      request.setIncludeAttributes("uid");
    } else {
      source.addAttributes(testLdapEntry.getAttributes());

      final LdapAttribute gn = new LdapAttribute("givenName");
      gn.addStringValues(testLdapEntry.getAttribute("givenName").getStringValues());
      gn.addStringValues("John");
      source.addAttributes(gn);

      final LdapAttribute initials = new LdapAttribute("initials");
      initials.addStringValues(testLdapEntry.getAttribute("initials").getStringValues());
      initials.addStringValues("JC");
      source.addAttributes(initials);
      request.setExcludeAttributes("givenName", "initials");
    }
    // no-op, include/exclude should prevent a modify from occurring
    merge.execute(request);

    final SearchOperation search = new SearchOperation(cf);
    SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(testLdapEntry.getDn(), testLdapEntry.getAttributeNames()));
    TestUtils.assertEquals(testLdapEntry, result.getEntry());

    if (TestControl.isActiveDirectory()) {
      request.setIncludeAttributes("givenName", "initials");
    } else {
      request.setExcludeAttributes((String[]) null);
    }
    // merge givenName and initials changes
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());

    // delete mail attribute
    final LdapAttribute mail = source.getAttribute("mail");
    source.removeAttributes(mail);
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertNull(result.getEntry().getAttribute("mail"));

    // add mail attribute
    source.addAttributes(mail);
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertNotNull(result.getEntry().getAttribute("mail"));

    // add new mail values using replace
    mail.addStringValues(
      "ccoolidge2@ldaptive.org", "ccoolidge3@ldaptive.org", "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 5);

    // remove mail values using replace
    mail.removeStringValues("ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 3);

    // add new mail value using add
    request.setUseReplace(false);
    mail.clear();
    mail.addStringValues("ccoolidge@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 1);

    mail.addStringValues(
      "ccoolidge2@ldaptive.org", "ccoolidge3@ldaptive.org", "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 5);

    // remove mail values using delete
    mail.removeStringValues("ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 3);

    // use batching
    request.setAttributeValuesBatchSize(2);
    request.setModificationBatchSize(1);
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 11);

    request.setAttributeValuesBatchSize(2);
    request.setModificationBatchSize(0);
    mail.removeStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 3);

    request.setAttributeValuesBatchSize(0);
    request.setModificationBatchSize(2);
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 11);

    request.setAttributeValuesBatchSize(2);
    request.setModificationBatchSize(10);
    mail.removeStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 3);

    request.setAttributeValuesBatchSize(1);
    request.setModificationBatchSize(10);
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
    Assert.assertEquals(result.getEntry().getAttribute("mail").size(), 11);
  }
}
