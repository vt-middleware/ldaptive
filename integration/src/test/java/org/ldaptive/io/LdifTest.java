/*
  $Id: LdifTest.java 2982 2014-05-19 19:34:28Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2982 $
  Updated: $Date: 2014-05-19 15:34:28 -0400 (Mon, 19 May 2014) $
*/
package org.ldaptive.io;

import java.io.StringReader;
import java.io.StringWriter;
import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdifReader} and {@link LdifWriter}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2982 $
 */
public class LdifTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry14")
  @BeforeClass(groups = {"ldif"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"ldif"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
      "ldifSearchDn",
      "ldifSearchFilter"
    })
  @Test(groups = {"ldif"})
  public void searchAndCompareLdif(final String dn, final String filter)
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final SearchOperation search = new SearchOperation(conn);

      final SearchRequest request =
        new SearchRequest(dn, new SearchFilter(filter));
      if (TestControl.isActiveDirectory()) {
        request.setBinaryAttributes("objectSid", "objectGUID", "jpegPhoto");
      }
      final SearchResult result1 = search.execute(request).getResult();

      final StringWriter writer = new StringWriter();
      final LdifWriter ldifWriter = new LdifWriter(writer);
      ldifWriter.write(result1);

      final StringReader reader = new StringReader(writer.toString());
      final LdifReader ldifReader = new LdifReader(reader);
      final SearchResult result2 = ldifReader.read();

      TestUtils.assertEquals(result2, result1);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  ldifFile  to create with
   * @param  ldifSortedFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
      "ldifEntry",
      "ldifSortedEntry"
    })
  @Test(groups = {"ldif"})
  public void readAndCompareSortedLdif(
    final String ldifFile,
    final String ldifSortedFile)
    throws Exception
  {
    final String ldifStringSorted = TestUtils.readFileIntoString(ldifSortedFile);
    final LdifReader ldifReader = new LdifReader(
      new StringReader(TestUtils.readFileIntoString(ldifFile)),
      SortBehavior.SORTED);
    final SearchResult result = ldifReader.read();

    final StringWriter writer = new StringWriter();
    final LdifWriter ldifWriter = new LdifWriter(writer);
    ldifWriter.write(result);

    AssertJUnit.assertEquals(ldifStringSorted, writer.toString());
  }


  /**
   * @param  ldifFileIn  to create with
   * @param  ldifFileOut  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
      "multipleLdifResultsIn",
      "multipleLdifResultsOut"
    })
  @Test(groups = {"ldif"})
  public void readAndCompareMultipleLdif(
    final String ldifFileIn,
    final String ldifFileOut)
    throws Exception
  {
    final String ldifStringIn = TestUtils.readFileIntoString(ldifFileIn);
    LdifReader ldifReader = new LdifReader(new StringReader(ldifStringIn));
    final SearchResult result1 = ldifReader.read();

    final String ldifStringOut = TestUtils.readFileIntoString(ldifFileOut);
    ldifReader = new LdifReader(new StringReader(ldifStringOut));
    final SearchResult result2 = ldifReader.read();

    AssertJUnit.assertEquals(result1, result2);
  }
}
