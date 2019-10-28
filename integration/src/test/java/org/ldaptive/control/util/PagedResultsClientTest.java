/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.Iterator;
import org.ldaptive.AbstractTest;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PagedResultsClient}.
 *
 * @author  Middleware Services
 */
public class PagedResultsClientTest extends AbstractTest
{

  /** Entries created for ldap tests. */
  private static LdapEntry[] testLdapEntries;


  /**
   * @param  ldifFile1  to create.
   * @param  ldifFile2  to create.
   * @param  ldifFile3  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "createEntry22",
      "createEntry23",
      "createEntry25"
    })
  @BeforeClass(groups = "control-util")
  public void createLdapEntry(final String ldifFile1, final String ldifFile2, final String ldifFile3)
    throws Exception
  {
    testLdapEntries = new LdapEntry[3];
    testLdapEntries[0] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile1)).getEntry();
    super.createLdapEntry(testLdapEntries[0]);
    testLdapEntries[1] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile2)).getEntry();
    super.createLdapEntry(testLdapEntries[1]);
    testLdapEntries[2] = TestUtils.convertLdifToResult(TestUtils.readFileIntoString(ldifFile3)).getEntry();
    super.createLdapEntry(testLdapEntries[2]);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "control-util")
  public void deleteLdapEntry()
    throws Exception
  {
    for (LdapEntry testLdapEntry : testLdapEntries) {
      super.deleteLdapEntry(testLdapEntry.getDn());
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
      "prSearchDn",
      "prSearchFilter"
    })
  @Test(groups = "control-util")
  public void execute(final String dn, final String filter)
    throws Exception
  {
    try (SingleConnectionFactory cf = TestUtils.createSingleConnectionFactory()) {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);

      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));
      SearchResponse response = client.execute(request);
      Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
      Assert.assertEquals(response.entrySize(), 1);
      Assert.assertEquals(response.getEntry().getDn().toLowerCase(), testLdapEntries[0].getDn().toLowerCase());

      int i = 1;
      while (client.hasMore(response)) {
        response = client.execute(request, response);
        Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
        Assert.assertEquals(response.entrySize(), 1);
        Assert.assertEquals(response.getEntry().getDn().toLowerCase(), testLdapEntries[i].getDn().toLowerCase());
        i++;
      }

      try {
        client.execute(request, response);
      } catch (IllegalArgumentException e) {
        Assert.assertNotNull(e);
      }
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
      "prSearchDn",
      "prSearchFilter"
    })
  @Test(groups = "control-util")
  public void executeToCompletion(final String dn, final String filter)
    throws Exception
  {
    try (SingleConnectionFactory cf = TestUtils.createSingleConnectionFactory()) {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);

      final SearchRequest request = new SearchRequest(dn, new SearchFilter(filter));

      final SearchResponse response = SearchResponse.sort(client.executeToCompletion(request));
      Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
      Assert.assertEquals(response.entrySize(), 3);

      final Iterator<LdapEntry> i = response.getEntries().iterator();
      Assert.assertEquals(response.getResultCode(), ResultCode.SUCCESS);
      Assert.assertEquals(i.next().getDn().toLowerCase(), testLdapEntries[1].getDn().toLowerCase());
      Assert.assertEquals(i.next().getDn().toLowerCase(), testLdapEntries[0].getDn().toLowerCase());
      Assert.assertEquals(i.next().getDn().toLowerCase(), testLdapEntries[2].getDn().toLowerCase());
    }
  }
}
