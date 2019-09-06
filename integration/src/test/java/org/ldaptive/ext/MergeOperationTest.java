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
import org.testng.AssertJUnit;
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
    AssertJUnit.assertFalse(super.entryExists(cf, testLdapEntry));

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry));
    AssertJUnit.assertTrue(super.entryExists(cf, testLdapEntry));
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "merge")
  public void deleteLdapEntry()
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    AssertJUnit.assertTrue(super.entryExists(cf, testLdapEntry));

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry, true));
    AssertJUnit.assertFalse(super.entryExists(cf, testLdapEntry));
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
    merge.execute(request);

    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    TestUtils.assertEquals(source, result.getEntry());
  }
}
