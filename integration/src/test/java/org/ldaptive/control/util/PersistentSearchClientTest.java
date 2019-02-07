/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import org.ldaptive.AbstractTest;
import org.ldaptive.AttributeModification;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DnParser;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyDnOperation;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
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
  @BeforeClass(groups = "control-util")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
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
  @Parameters(
    {
      "persistentSearchDn",
      "persistentSearchReturnAttrs",
      "persistentSearchResults"
    })
  @Test(groups = "control-util")
  public void persistentSearch(final String dn, final String returnAttrs, final String ldifFile)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final String expected = TestUtils.readFileIntoString(ldifFile);
    final SearchResponse expectedResult = TestUtils.convertLdifToResult(expected);

    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final PersistentSearchClient client = new PersistentSearchClient(
      cf,
      EnumSet.allOf(PersistentSearchChangeType.class),
      true,
      true);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, returnAttrs.split("\\|"));
    final BlockingQueue<PersistentSearchItem> results = client.execute(request);

    // test the async request
    PersistentSearchItem item = results.take();
    if (item.isException()) {
      throw item.getException();
    }
    checkItem(item);

    // make a change
    final LdapAttribute modAttr = new LdapAttribute("initials", "PSC");
    final ModifyOperation modify = new ModifyOperation(cf);
    modify.execute(new ModifyRequest(dn, new AttributeModification(AttributeModification.Type.REPLACE, modAttr)));
    item = results.take();
    checkItem(item);
    AssertJUnit.assertTrue(item.isEntry());
    AssertJUnit.assertEquals(
      PersistentSearchChangeType.MODIFY,
      item.getEntry().getEntryChangeNotificationControl().getChangeType());
    expectedResult.getEntry().addAttributes(modAttr);
    TestUtils.assertEquals(
      expectedResult.getEntry(),
      createCompareEntry(expectedResult.getEntry(), item.getEntry().getSearchEntry()));

    // modify dn
    final String modDn = "CN=PSC," + DnParser.substring(dn, 1);
    final LdapAttribute cn = expectedResult.getEntry().getAttribute("cn");
    final ModifyDnOperation modifyDn = new ModifyDnOperation(cf);
    modifyDn.execute(new ModifyDnRequest(dn, DnParser.substring(modDn, 0, 1), true));
    item = results.take();
    AssertJUnit.assertTrue(item.isEntry());
    AssertJUnit.assertEquals(
      PersistentSearchChangeType.MODDN,
      item.getEntry().getEntryChangeNotificationControl().getChangeType());
    expectedResult.getEntry().setDn(modDn);
    expectedResult.getEntry().addAttributes(new LdapAttribute("CN", "PSC"));
    TestUtils.assertEquals(
      expectedResult.getEntry(),
      createCompareEntry(expectedResult.getEntry(), item.getEntry().getSearchEntry()));

    // modify dn back
    modifyDn.execute(new ModifyDnRequest(modDn, DnParser.substring(dn, 0, 1), true));
    item = results.take();
    AssertJUnit.assertTrue(item.isEntry());
    AssertJUnit.assertEquals(
      PersistentSearchChangeType.MODDN,
      item.getEntry().getEntryChangeNotificationControl().getChangeType());
    expectedResult.getEntry().setDn(dn);
    expectedResult.getEntry().addAttributes(cn);
    TestUtils.assertEquals(
      expectedResult.getEntry(),
      createCompareEntry(expectedResult.getEntry(), item.getEntry().getSearchEntry()));

    client.abandon();
    if (!results.isEmpty()) {
      item = results.take();
      if (item.isResult()) {
        AssertJUnit.assertEquals(ResultCode.USER_CANCELLED, item.getResult().getResultCode());
      } else {
        AssertJUnit.fail("Unknown result type");
      }
    }
    AssertJUnit.assertTrue(results.isEmpty());
  }


  /**
   * Check that the supplied item isn't a response or exception.
   *
   * @param  item  to check
   *
   * @throws  UnsupportedOperationException  if the server doesn't support this control
   * @throws  IllegalStateException  if the item is a response or exception
   */
  private void checkItem(final PersistentSearchItem item)
  {
    if (item.isResult()) {
      if (ResultCode.UNAVAILABLE_CRITICAL_EXTENSION.equals(item.getResult().getResultCode())) {
        // ignore this test if not supported by the server
        throw new UnsupportedOperationException("LDAP server does not support this control");
      } else {
        throw new IllegalStateException("Unexpected response: " + item.getResult());
      }
    } else if (item.isException()) {
      throw new IllegalStateException("Unexpected exception: " + item.getException());
    }
  }


  /**
   * Creates an ldap entry to compare with expected results. Removes any extra attributes returned from using the
   * persistent search control.
   *
   * @param  expectedEntry  to compare with
   * @param  searchEntry  returned from a persistent search
   *
   * @return  sanitized entry to compare with
   */
  private LdapEntry createCompareEntry(final LdapEntry expectedEntry, final LdapEntry searchEntry)
  {
    final LdapEntry compareEntry = LdapEntry.builder().dn(expectedEntry.getDn()).build();
    for (String attr : expectedEntry.getAttributeNames()) {
      compareEntry.addAttributes(searchEntry.getAttribute(attr));
    }
    return compareEntry;
  }
}
