/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.Message;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.TestControl;
import org.ldaptive.control.SyncDoneControl;
import org.ldaptive.control.SyncStateControl;
import org.ldaptive.extended.SyncInfoMessage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link SyncReplClient}.
 *
 * @author  Middleware Services
 */
public class SyncReplClientTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry18")
  @BeforeClass(groups = "control-util")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntry = convertLdifToEntry(ldif);
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "control-util")
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "syncReplSearchDn",
    "syncReplSearchReturnAttrs",
    "syncReplSearchResults"
  })
  @Test(groups = "control-util")
  public void syncReplRefreshOnly(final String dn, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = readFileIntoString(ldifFile);

    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|"));
      final SyncReplClient client = new SyncReplClient(cf, false);
      final BlockingQueue<Object> queue = new LinkedBlockingDeque<>();
      client.setOnException(queue::add);
      client.setOnEntry(queue::add);
      client.setOnMessage(queue::add);
      client.setOnResult(queue::add);
      client.send(request, new DefaultCookieManager());

      final LdapEntry entry = (LdapEntry) queue.take();
      assertThat(entry).isNotNull();
      final SyncStateControl ssc = (SyncStateControl) entry.getControl(SyncStateControl.OID);
      assertThat(ssc.getSyncState()).isEqualTo(SyncStateControl.State.ADD);
      assertThat(ssc.getEntryUuid()).isNotNull();
      assertThat(ssc.getCookie()).isNull();

      final Result result = (Result) queue.take();
      assertThat(result).isNotNull();
      final SyncDoneControl sdc = (SyncDoneControl) result.getControl(SyncDoneControl.OID);
      assertThat(sdc.getRefreshDeletes()).isTrue();
      assertThat(sdc.getCookie()).isNotNull();
    } finally {
      cf.close();
    }
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({
    "syncReplSearchDn",
    "syncReplSearchReturnAttrs",
    "syncReplSearchResults"
  })
  @Test(groups = "control-util")
  public void syncReplRefreshAndPersist(final String dn, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = readFileIntoString(ldifFile);

    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|"));
      final SyncReplClient client = new SyncReplClient(cf, true);
      final BlockingQueue<Object> queue = new LinkedBlockingDeque<>();
      client.setOnException(queue::add);
      client.setOnEntry(queue::add);
      client.setOnMessage(queue::add);
      client.setOnResult(queue::add);
      client.send(request, new DefaultCookieManager());

      LdapEntry entry = (LdapEntry) queue.take();
      assertThat(entry).isNotNull();
      SyncStateControl ssc = (SyncStateControl) entry.getControl(SyncStateControl.OID);
      assertThat(ssc.getSyncState()).isEqualTo(SyncStateControl.State.ADD);
      assertThat(ssc.getEntryUuid()).isNotNull();
      assertThat(ssc.getCookie()).isNull();
      // TODO this will need some work
      LdapEntryAssert.assertThat(entry).isSame(convertLdifToEntry(expected));

      final Message message = (Message) queue.take();
      assertThat(message).isNotNull();
      final SyncInfoMessage sim = (SyncInfoMessage) message;
      assertThat(sim.getMessageType()).isEqualTo(SyncInfoMessage.Type.REFRESH_DELETE);
      assertThat(sim.getCookie()).isNotNull();
      assertThat(sim.getRefreshDeletes()).isFalse();
      assertThat(sim.getRefreshDone()).isTrue();

      // make a change
      final ModifyOperation modify = new ModifyOperation(cf);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(AttributeModification.Type.ADD, new LdapAttribute("employeeType", "Employee"))));

      entry = (LdapEntry) queue.take();
      assertThat(entry).isNotNull();
      ssc = (SyncStateControl) entry.getControl(SyncStateControl.OID);
      assertThat(ssc.getSyncState()).isEqualTo(SyncStateControl.State.MODIFY);
      assertThat(ssc.getEntryUuid()).isNotNull();
      assertThat(ssc.getCookie()).isNotNull();

      // change it back
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(AttributeModification.Type.DELETE, new LdapAttribute("employeeType"))));

      entry = (LdapEntry) queue.take();
      assertThat(entry).isNotNull();
      ssc = (SyncStateControl) entry.getControl(SyncStateControl.OID);
      assertThat(ssc.getSyncState()).isEqualTo(SyncStateControl.State.MODIFY);
      assertThat(ssc.getEntryUuid()).isNotNull();
      assertThat(ssc.getCookie()).isNotNull();
      // TODO this will need some work
      LdapEntryAssert.assertThat(entry).isSame(convertLdifToEntry(expected));

      client.cancel();

      final Result result = (Result) queue.take();
      assertThat(result).isNotNull();
      assertThat(result.getResultCode()).isEqualTo(ResultCode.CANCELED);
    } finally {
      cf.close();
    }
  }
}
