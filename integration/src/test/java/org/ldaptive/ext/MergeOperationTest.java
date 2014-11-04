/*
  $Id: MergeOperationTest.java 2934 2014-03-28 16:41:39Z dfisher $

  Copyright (C) 2003-2013 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2934 $
  Updated: $Date: 2014-03-28 12:41:39 -0400 (Fri, 28 Mar 2014) $
*/
package org.ldaptive.ext;

import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
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
 * @version  $Revision: 2934 $
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
  @BeforeClass(groups = {"merge"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      AssertJUnit.assertFalse(super.entryExists(conn, testLdapEntry));
      final MergeOperation merge = new MergeOperation(conn);
      merge.execute(new MergeRequest(testLdapEntry));
      AssertJUnit.assertTrue(super.entryExists(conn, testLdapEntry));
    } finally {
      conn.close();
    }
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"merge"})
  public void deleteLdapEntry()
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      AssertJUnit.assertTrue(super.entryExists(conn, testLdapEntry));
      final MergeOperation merge = new MergeOperation(conn);
      merge.execute(new MergeRequest(testLdapEntry, true));
      AssertJUnit.assertFalse(super.entryExists(conn, testLdapEntry));
      merge.execute(new MergeRequest(testLdapEntry, true));
    } finally {
      conn.close();
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"merge"})
  public void merge()
    throws Exception
  {
    final LdapEntry source = new LdapEntry(testLdapEntry.getDn());

    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final MergeOperation merge = new MergeOperation(conn);
      final MergeRequest request = new MergeRequest(source);
      if (TestControl.isActiveDirectory()) {
        // remove objectClass for comparison testing related to AD
        testLdapEntry.removeAttribute("objectClass");
        source.addAttributes(testLdapEntry.getAttributes());
        // these attributes are single value in AD
        source.addAttribute(new LdapAttribute("givenName", "John"));
        source.addAttribute(new LdapAttribute("initials", "JC"));
        request.setIncludeAttributes("uid");
      } else {
        source.addAttributes(testLdapEntry.getAttributes());
        final LdapAttribute gn = new LdapAttribute("givenName");
        gn.addStringValues(
          testLdapEntry.getAttribute("givenName").getStringValues());
        gn.addStringValue("John");
        source.addAttribute(gn);
        final LdapAttribute initials = new LdapAttribute("initials");
        initials.addStringValues(
          testLdapEntry.getAttribute("initials").getStringValues());
        initials.addStringValue("JC");
        source.addAttribute(initials);
        request.setExcludeAttributes("givenName", "initials");
      }
      // no-op, include/exclude should prevent a modify from occurring
      merge.execute(request);

      final SearchOperation search = new SearchOperation(conn);
      SearchResult result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(
          testLdapEntry.getDn(),
          testLdapEntry.getAttributeNames())).getResult();
      TestUtils.assertEquals(testLdapEntry, result.getEntry());

      if (TestControl.isActiveDirectory()) {
        request.setIncludeAttributes("givenName", "initials");
      } else {
        request.setExcludeAttributes((String[]) null);
      }
      merge.execute(request);

      result = search.execute(
        SearchRequest.newObjectScopeSearchRequest(
          source.getDn(), source.getAttributeNames())).getResult();
      TestUtils.assertEquals(source, result.getEntry());
    } finally {
      conn.close();
    }
  }
}
