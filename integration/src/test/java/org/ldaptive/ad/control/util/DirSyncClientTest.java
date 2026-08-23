/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ad.control.util;

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
import org.ldaptive.ad.control.DirSyncControl;
import org.ldaptive.ad.handler.ObjectGuidHandler;
import org.ldaptive.pool.QueueType;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link DirSyncClient}.
 *
 * @author  Middleware Services
 */
public class DirSyncClientTest extends AbstractTest
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
    "createEntry27",
    "createEntry28",
    "createEntry29"
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void execute(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeWithPooledConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeWithDefaultConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletion(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletionWithPooledConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    "dsSearchDn",
    "dsSearchFilter"
  })
  @Test(groups = "control-util")
  public void executeToCompletionWithDefaultConnectionFactory(final String dn, final String filter)
    throws Exception
  {
    if (!TestControl.isActiveDirectory()) {
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
    final DirSyncClient client = new DirSyncClient(
      cf,
      new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });
    client.setEntryHandlers(new ObjectGuidHandler());

    final SearchRequest request = new SearchRequest(dn.substring(dn.indexOf(",") + 1), filter, "uid");
    final SearchResponse response = client.execute(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isGreaterThan(0);
    assertThat(client.hasMore(response)).isFalse();
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
    final DirSyncClient client = new DirSyncClient(
      cf,
      new DirSyncControl.Flag[] {DirSyncControl.Flag.ANCESTORS_FIRST_ORDER, });
    client.setEntryHandlers(new ObjectGuidHandler());

    final SearchRequest request = new SearchRequest(dn.substring(dn.indexOf(",") + 1), filter, "uid");
    final SearchResponse response = client.executeToCompletion(request);
    assertThat(response.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    assertThat(response.entrySize()).isGreaterThan(0);
  }
}
