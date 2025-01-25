/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.Iterator;
import org.ldaptive.AbstractTest;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.dn.Dn;
import org.ldaptive.handler.AbandonOperationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

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
  @Parameters({
    "createEntry22",
    "createEntry23",
    "createEntry25"
  })
  @BeforeClass(groups = "control-util")
  public void createLdapEntry(final String ldifFile1, final String ldifFile2, final String ldifFile3)
    throws Exception
  {
    testLdapEntries = new LdapEntry[3];
    testLdapEntries[0] = convertLdifToEntry(readFileIntoString(ldifFile1));
    super.createLdapEntry(testLdapEntries[0]);
    testLdapEntries[1] = convertLdifToEntry(readFileIntoString(ldifFile2));
    super.createLdapEntry(testLdapEntries[1]);
    testLdapEntries[2] = convertLdifToEntry(readFileIntoString(ldifFile3));
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
  @Parameters({
    "prSearchDn",
    "prSearchFilter"
  })
  @Test(groups = "control-util")
  public void execute(final String dn, final String filter)
    throws Exception
  {
    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);

      final SearchRequest request = new SearchRequest(dn, filter);
      SearchResponse response = client.execute(request);
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(response.entrySize()).isEqualTo(1);
      assertThat(new Dn(response.getEntry().getDn()).format()).isEqualTo(new Dn(testLdapEntries[0].getDn()).format());

      int i = 1;
      while (client.hasMore(response)) {
        response = client.execute(request, response);
        assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
        assertThat(response.entrySize()).isEqualTo(1);
        assertThat(new Dn(response.getEntry().getDn()).format()).isEqualTo(new Dn(testLdapEntries[i].getDn()).format());
        i++;
      }

      try {
        client.execute(request, response);
      } catch (IllegalArgumentException e) {
        assertThat(e).isNotNull();
      }
    } finally {
      cf.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "prSearchDn",
    "prSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletion(final String dn, final String filter)
    throws Exception
  {
    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);

      final SearchRequest request = new SearchRequest(dn, filter);

      final SearchResponse response = SearchResponse.sort(client.executeToCompletion(request));
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(response.entrySize()).isEqualTo(3);

      final Iterator<LdapEntry> i = response.getEntries().iterator();
      assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
      assertThat(new Dn(i.next().getDn()).format()).isEqualTo(new Dn(testLdapEntries[1].getDn()).format());
      assertThat(new Dn(i.next().getDn()).format()).isEqualTo(new Dn(testLdapEntries[0].getDn()).format());
      assertThat(new Dn(i.next().getDn()).format()).isEqualTo(new Dn(testLdapEntries[2].getDn()).format());
    } finally {
      cf.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "prSearchDn",
    "prSearchFilter"
  })
  @Test(groups = "control-util")
  public void throwsHandler(final String dn, final String filter)
    throws Exception
  {
    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);
      client.setEntryHandlers(le -> {
        throw new IllegalStateException("Test handler exception");
      });

      final SearchRequest request = new SearchRequest(dn, filter);
      try {
        client.executeToCompletion(request);
      } catch (Exception e) {
        fail("Should not have thrown exception");
      }
    } finally {
      cf.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "prSearchDn",
    "prSearchFilter"
  })
  @Test(groups = "control-util")
  public void throwsHandlerAbandon(final String dn, final String filter)
    throws Exception
  {
    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final PagedResultsClient client = new PagedResultsClient(cf, 1);
      client.setEntryHandlers(le -> {
        throw new IllegalStateException(new AbandonOperationException("Test handler exception"));
      });

      final SearchRequest request = new SearchRequest(dn, filter);
      try {
        client.executeToCompletion(request);
        fail("Should have thrown exception");
      } catch (Exception e) {
        assertThat(e).isExactlyInstanceOf(AbandonOperationException.class);
      }
    } finally {
      cf.close();
    }
  }
}
