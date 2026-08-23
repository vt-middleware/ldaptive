/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.Iterator;
import org.ldaptive.AbstractTest;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.SingleConnectionFactoryWrapper;
import org.ldaptive.TestControl;
import org.ldaptive.control.SortKey;
import org.ldaptive.pool.QueueType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link VirtualListViewClient}.
 *
 * @author  Middleware Services
 */
public class VirtualListViewClientTest extends AbstractTest
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
    "createEntry19",
    "createEntry20",
    "createEntry21"
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
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void execute(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      executeAssertions(cf, dn, filter);
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
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeWithPooledConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final PooledConnectionFactory factory = PooledConnectionFactory.builder()
      .config(readConnectionConfig(null))
      .build();
    factory.setQueueType(QueueType.FIFO);
    factory.initialize();
    try (SingleConnectionFactoryWrapper cf = new SingleConnectionFactoryWrapper(factory)) {
      executeAssertions(cf, dn, filter);
    } finally {
      factory.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeWithDefaultConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    try (SingleConnectionFactoryWrapper cf = new SingleConnectionFactoryWrapper(createConnectionFactory())) {
      executeAssertions(cf, dn, filter);
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletion(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      executeToCompletionAssertions(cf, dn, filter);
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
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletionWithPooledConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    final PooledConnectionFactory factory = PooledConnectionFactory.builder()
      .config(readConnectionConfig(null))
      .build();
    factory.setQueueType(QueueType.FIFO);
    factory.initialize();
    try (SingleConnectionFactoryWrapper cf = new SingleConnectionFactoryWrapper(factory)) {
      executeToCompletionAssertions(cf, dn, filter);
    } finally {
      factory.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "vlvSearchDn",
    "vlvSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletionWithDefaultConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    // AD server says vlv is a supported control, but returns UNAVAIL_EXTENSION
    if (TestControl.isActiveDirectory() || TestControl.isOracleDirectory()) {
      return;
    }

    try (SingleConnectionFactoryWrapper cf = new SingleConnectionFactoryWrapper(createConnectionFactory())) {
      executeToCompletionAssertions(cf, dn, filter);
    }
  }


  /**
   * @param  cf  connection factory
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  private void executeAssertions(final ConnectionFactory cf, final String dn, final String filter)
    throws Exception
  {
    VirtualListViewClient client = new VirtualListViewClient(
      cf,
      new SortKey("uid", "caseExactMatch"),
      new SortKey("givenName", "caseIgnoreMatch"));

    SearchRequest request = new SearchRequest(dn, filter);
    //request.setSortBehavior(SortBehavior.ORDERED);
    SearchResponse response = client.execute(request, new VirtualListViewParams(1, 0, 1));
    Iterator<LdapEntry> i = response.getEntries().iterator();
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isEqualTo(2);
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[0].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[1].getDn());

    response = client.execute(request, new VirtualListViewParams(2, 1, 1), response);
    i = response.getEntries().iterator();
    assertThat(ResultCode.SUCCESS).isEqualTo(response.getResultCode());
    assertThat(response.entrySize()).isEqualTo(3);
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[0].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[1].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[2].getDn());

    // VLV does not give a clean way to destroy the server side sorted entries
    // list. Open a new connection.
    client = new VirtualListViewClient(cf, new SortKey("uid", "caseExactMatch"));

    request = new SearchRequest(dn, filter);
    //request.setSortBehavior(SortBehavior.ORDERED);
    response = client.execute(request, new VirtualListViewParams("21", 1, 0));
    i = response.getEntries().iterator();
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isEqualTo(2);
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[1].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[2].getDn());

    response = client.execute(request, new VirtualListViewParams("19", 0, 2), response);
    i = response.getEntries().iterator();
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isEqualTo(3);
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[0].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[1].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[2].getDn());
  }


  /**
   * @param  cf  connection factory
   * @param  dn  to search on.
   * @param  filter  to search with.
   *
   * @throws  Exception  On test failure.
   */
  private void executeToCompletionAssertions(final ConnectionFactory cf, final String dn, final String filter)
    throws Exception
  {
    final VirtualListViewClient client = new VirtualListViewClient(
      cf,
      new SortKey("uid", "caseExactMatch"),
      new SortKey("givenName", "caseIgnoreMatch"));

    final SearchRequest request = new SearchRequest(dn, filter);
    final SearchResponse response = client.executeToCompletion(request, new VirtualListViewParams(1, 0, 1));
    final Iterator<LdapEntry> i = response.getEntries().iterator();
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isEqualTo(3);
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[2].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[0].getDn());
    assertThat(i.next().getDn()).isEqualTo(testLdapEntries[1].getDn());
  }
}
