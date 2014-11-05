/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.AttributeModificationType;
import org.ldaptive.Connection;
import org.ldaptive.DnParser;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.control.PersistentSearchChangeType;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PersistentSearchClient}.
 *
 * @author  Middleware Services
 */
public class PersistentSearchClientTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry31")
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
  @Parameters({
    "persistentSearchDn",
    "persistentSearchReturnAttrs",
    "persistentSearchResults"
  })
  @Test(groups = {"control-util"})
  public void persistentSearch(
    final String dn,
    final String returnAttrs,
    final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResult expectedResult = TestUtils.convertLdifToResult(expected);

    Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final PersistentSearchClient client = new PersistentSearchClient(
        conn,
        EnumSet.allOf(PersistentSearchChangeType.class),
        true,
        true);
      final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(
        dn, returnAttrs.split("\\|"));
      final BlockingQueue<PersistentSearchItem> results =
        client.execute(request);

      // test the async request
      PersistentSearchItem item = results.take();
      if (item.isException()) {
        throw item.getException();
      }
      AsyncRequest asyncRequest = null;
      if (item.isAsyncRequest()) {
        asyncRequest = item.getAsyncRequest();
        // some providers don't support the request object
        AssertJUnit.assertTrue(item.getAsyncRequest().getMessageId() > 0);
      }
      checkItem(item);

      // make a change
      final LdapAttribute modAttr = new LdapAttribute("initials", "PSC");
      ModifyOperation modify = new ModifyOperation(conn);
      modify.execute(new ModifyRequest(
        dn,
        new AttributeModification(
          AttributeModificationType.REPLACE,
          modAttr)));
      item = results.take();
      checkItem(item);
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(
        PersistentSearchChangeType.MODIFY,
        item.getEntry().getEntryChangeNotificationControl().getChangeType());
      expectedResult.getEntry().addAttribute(modAttr);
      TestUtils.assertEquals(
        expectedResult,
        new SearchResult(
          createCompareEntry(
            expectedResult.getEntry(), item.getEntry().getSearchEntry())));

      // modify dn
      final String modDn = "CN=PSC," + DnParser.substring(dn, 1);
      final LdapAttribute cn = expectedResult.getEntry().getAttribute("cn");
      ModifyDnOperation modifyDn = new ModifyDnOperation(conn);
      modifyDn.execute(new ModifyDnRequest(dn, modDn));
      item = results.take();
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(
        PersistentSearchChangeType.MODDN,
        item.getEntry().getEntryChangeNotificationControl().getChangeType());
      expectedResult.getEntry().setDn(modDn);
      expectedResult.getEntry().addAttribute(new LdapAttribute("CN", "PSC"));
      TestUtils.assertEquals(
        expectedResult,
        new SearchResult(
          createCompareEntry(
            expectedResult.getEntry(), item.getEntry().getSearchEntry())));

      // modify dn back
      modifyDn.execute(new ModifyDnRequest(modDn, dn));
      item = results.take();
      AssertJUnit.assertTrue(item.isEntry());
      AssertJUnit.assertEquals(
        PersistentSearchChangeType.MODDN,
        item.getEntry().getEntryChangeNotificationControl().getChangeType());
      expectedResult.getEntry().setDn(dn);
      expectedResult.getEntry().addAttribute(cn);
      TestUtils.assertEquals(
        expectedResult,
        new SearchResult(
          createCompareEntry(
            expectedResult.getEntry(), item.getEntry().getSearchEntry())));

      asyncRequest.abandon();
      if (!results.isEmpty()) {
        item = results.take();
        if (item.isResponse()) {
          AssertJUnit.assertEquals(
            ResultCode.USER_CANCELLED,
            item.getResponse().getResultCode());
        } else if (item.isException()) {
          final LdapException e = (LdapException) item.getException();
          AssertJUnit.assertEquals(
            ResultCode.USER_CANCELLED,
            e.getResultCode());
        } else {
          AssertJUnit.fail("Unknown result type");
        }
      }
      AssertJUnit.assertTrue(results.isEmpty());
    } finally {
      conn.close();
    }
  }


  /**
   * Check that the supplied item isn't a response or exception.
   *
   * @param  item  to check
   *
   * @throws  UnsupportedOperationException  if the server doesn't support this
   * control
   * @throws  IllegalStateException  if the item is a response or exception
   */
  private void checkItem(final PersistentSearchItem item)
  {
    if (item.isResponse()) {
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION.equals(
        item.getResponse().getResultCode())) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException(
          "LDAP server does not support this control");
      } else {
        throw new IllegalStateException(
          "Unexpected response: " + item.getResponse());
      }
    } else if (item.isException()) {
      final LdapException e = (LdapException) item.getException();
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION.equals(
        e.getResultCode())) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException(
          "LDAP server does not support this control");
      } else {
        throw new IllegalStateException("Unexpected exception: " + e);
      }
    }
  }


  /**
   * Creates an ldap entry to compare with expected results. Removes any extra
   * attributes returned from using the persistent search control.
   *
   * @param  expectedEntry  to compare with
   * @param  searchEntry  returned from a persistent search
   *
   * @return  sanitized entry to compare with
   *
   * @throws  Exception  if an error occurs
   */
  private LdapEntry createCompareEntry(
    final LdapEntry expectedEntry,
    final SearchEntry searchEntry)
    throws Exception
  {
    final LdapEntry compareEntry = new LdapEntry(expectedEntry.getDn());
    for (String attr : expectedEntry.getAttributeNames()) {
      compareEntry.addAttribute(searchEntry.getAttribute(attr));
    }
    return compareEntry;
  }
}
