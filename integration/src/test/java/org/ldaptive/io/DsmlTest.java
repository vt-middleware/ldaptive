/* See LICENSE for licensing and NOTICE for copyright. */
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
 * Unit test for {@link Dsmlv1Reader} and {@link Dsmlv1Writer}.
 *
 * @author  Middleware Services
 */
public class DsmlTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry13")
  @BeforeClass(groups = {"dsml"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"dsml"})
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
  @Parameters(
    {
      "dsmlSearchDn",
      "dsmlSearchFilter"
    })
  @Test(groups = {"dsml"})
  public void searchAndCompareDsmlv1(final String dn, final String filter)
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);

      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
      if (TestControl.isActiveDirectory()) {
        request.setBinaryAttributes("objectSid", "objectGUID", "jpegPhoto");
      } else {
        request.setBinaryAttributes("jpegPhoto");
      }

      final SearchResult result1 = search.execute(request).getResult();

      final StringWriter writer = new StringWriter();
      final Dsmlv1Writer dsmlWriter = new Dsmlv1Writer(writer);
      dsmlWriter.write(result1);

      final StringReader reader = new StringReader(writer.toString());
      final Dsmlv1Reader dsmlReader = new Dsmlv1Reader(reader);
      final SearchResult result2 = dsmlReader.read();

      TestUtils.assertEquals(result2, result1);
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dsmlFile  to test with.
   * @param  dsmlSortedFile  to test with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "dsmlv1Entry",
      "dsmlv1SortedEntry"
    })
  @Test(groups = {"dsml"})
  public void readAndCompareDsmlv1(final String dsmlFile, final String dsmlSortedFile)
    throws Exception
  {
    final String dsmlStringSorted = TestUtils.readFileIntoString(dsmlSortedFile);
    final Dsmlv1Reader dsmlReader = new Dsmlv1Reader(
      new StringReader(TestUtils.readFileIntoString(dsmlFile)),
      SortBehavior.SORTED);
    final SearchResult result = dsmlReader.read();

    final StringWriter writer = new StringWriter();
    final Dsmlv1Writer dsmlWriter = new Dsmlv1Writer(writer);
    dsmlWriter.write(result);

    AssertJUnit.assertEquals(dsmlStringSorted, writer.toString());
  }
}
