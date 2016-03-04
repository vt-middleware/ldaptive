/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.concurrent.BlockingQueue;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.control.SyncStateControl;
import org.ldaptive.intermediate.SyncInfoMessage;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

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
  @BeforeClass(groups = {"control-util"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"control-util"})
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
  @Parameters(
    {
      "syncReplSearchDn",
      "syncReplSearchReturnAttrs",
      "syncReplSearchResults"
    })
  @Test(groups = {"control-util"})
  public void syncReplRefreshOnly(final String dn, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final SyncReplClient client = new SyncReplClient(conn, false);
      final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(dn, returnAttrs.split("\\|"));
      final BlockingQueue<SyncReplItem> results = client.execute(request, new DefaultCookieManager());

      SyncReplItem item = results.take();
      if (item.isException()) {
        throw item.getException();
      }
      if (item.isAsyncRequest()) {
        // some providers don't support the request object
        AssertJUnit.assertTrue(item.isAsyncRequest());
        AssertJUnit.assertTrue(item.getAsyncRequest().getMessageId() > 0);
        item = results.take();
      }
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(SyncStateControl.State.ADD, item.getEntry().getSyncStateControl().getSyncState());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getEntryUuid());
      AssertJUnit.assertNull(item.getEntry().getSyncStateControl().getCookie());

      item = results.take();
      AssertJUnit.assertTrue(item.isResponse());
      AssertJUnit.assertEquals(true, item.getResponse().getSyncDoneControl().getRefreshDeletes());
      AssertJUnit.assertNotNull(item.getResponse().getSyncDoneControl().getCookie());
    }
  }


  /**
   * @param  dn  to search on.
   * @param  returnAttrs  to return from search.
   * @param  ldifFile  to compare with
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "syncReplSearchDn",
      "syncReplSearchReturnAttrs",
      "syncReplSearchResults"
    })
  @Test(groups = {"control-util"})
  public void syncReplRefreshAndPersist(final String dn, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    // provider doesn't support cancel
    if (TestControl.isApacheProvider()) {
      throw new UnsupportedOperationException("Apache LDAP does not support cancel");
    }
    if (TestControl.isOpenDJProvider()) {
      throw new UnsupportedOperationException("OpenDJ does not support cancel");
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);

    try (Connection conn = TestUtils.createConnection()) {
      conn.open();

      final SyncReplClient client = new SyncReplClient(conn, true);
      final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(dn, returnAttrs.split("\\|"));
      final BlockingQueue<SyncReplItem> results = client.execute(request, new DefaultCookieManager());

      // test the async request
      SyncReplItem item = results.take();
      if (item.isException()) {
        throw item.getException();
      }
      if (item.isAsyncRequest()) {
        // some providers don't support the request object
        AssertJUnit.assertTrue(item.isAsyncRequest());
        AssertJUnit.assertTrue(item.getAsyncRequest().getMessageId() > 0);
        item = results.take();
      }

      // test the first entry
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(SyncStateControl.State.ADD, item.getEntry().getSyncStateControl().getSyncState());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getEntryUuid());
      AssertJUnit.assertNull(item.getEntry().getSyncStateControl().getCookie());
      TestUtils.assertEquals(
        TestUtils.convertLdifToResult(expected),
        new SearchResult(item.getEntry().getSearchEntry()));

      // test the info message
      item = results.take();
      if (item.isException()) {
        throw item.getException();
      }
      AssertJUnit.assertTrue(item.isMessage());
      AssertJUnit.assertEquals(SyncInfoMessage.Type.REFRESH_DELETE, item.getMessage().getMessageType());
      AssertJUnit.assertNotNull(item.getMessage().getCookie());
      AssertJUnit.assertFalse(item.getMessage().getRefreshDeletes());
      AssertJUnit.assertTrue(item.getMessage().getRefreshDone());

      // make a change
      final ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(AttributeModificationType.ADD, new LdapAttribute("employeeType", "Employee"))));
      item = results.take();
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(SyncStateControl.State.MODIFY, item.getEntry().getSyncStateControl().getSyncState());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getEntryUuid());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getCookie());

      // change it back
      modify.execute(
        new ModifyRequest(
          dn,
          new AttributeModification(AttributeModificationType.REMOVE, new LdapAttribute("employeeType"))));
      item = results.take();
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(SyncStateControl.State.MODIFY, item.getEntry().getSyncStateControl().getSyncState());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getEntryUuid());
      AssertJUnit.assertNotNull(item.getEntry().getSyncStateControl().getCookie());
      TestUtils.assertEquals(
        TestUtils.convertLdifToResult(expected),
        new SearchResult(item.getEntry().getSearchEntry()));

      client.cancel(item.getEntry().getSearchEntry().getMessageId());

      item = results.take();
      if (item.isException()) {
        throw item.getException();
      }
      AssertJUnit.assertTrue(item.isResponse());
      AssertJUnit.assertTrue(results.isEmpty());
      AssertJUnit.assertEquals(ResultCode.CANCELED, item.getResponse().getResponse().getResultCode());
    }
  }
}
