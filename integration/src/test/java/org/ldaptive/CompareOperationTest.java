/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CompareOperation}.
 *
 * @author  Middleware Services
 */
public class CompareOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry3")
  @BeforeClass(groups = {"compare"})
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = {"compare"})
  public void deleteLdapEntry()
    throws Exception
  {
    super.deleteLdapEntry(testLdapEntry.getDn());
  }


  /**
   * @param  dn  to compare.
   * @param  attrName  to compare with.
   * @param  attrValue  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "compareDn", "compareAttrName", "compareAttrValue" })
  @Test(
    groups = {"compare"},
    threadPoolSize = TEST_THREAD_POOL_SIZE,
    invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT
  )
  public void compare(
    final String dn,
    final String attrName,
    final String attrValue)
    throws Exception
  {
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final CompareOperation compare = new CompareOperation(conn);
      LdapAttribute la = new LdapAttribute();
      la.setName("cn");
      la.addStringValue("not-a-name");
      AssertJUnit.assertFalse(
        compare.execute(new CompareRequest(dn, la)).getResult());

      la = new LdapAttribute();
      la.setName(attrName);
      la.addStringValue(attrValue);
      AssertJUnit.assertTrue(
        compare.execute(new CompareRequest(dn, la)).getResult());
    } finally {
      conn.close();
    }
  }


  /**
   * @param  dn  to compare.
   * @param  attrName  to compare with.
   * @param  attrValue  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters(
    {
      "compareReferralDn",
      "compareReferralAttrName",
      "compareReferralAttrValue"
    }
  )
  @Test(groups = {"compare"})
  public void compareReferral(
    final String dn,
    final String attrName,
    final String attrValue)
  throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    }

    // expects a referral on the dn ou=referrals
    final String referralDn = "ou=referrals," + DnParser.substring(dn, 1);
    final Connection conn = TestUtils.createConnection();
    try {
      conn.open();
      final CompareOperation compare = new CompareOperation(conn);
      try {
        final CompareRequest request = new CompareRequest(
          referralDn, new LdapAttribute(attrName, attrValue));
        request.setFollowReferrals(false);
        Response<Boolean> response = compare.execute(request);
        AssertJUnit.assertEquals(ResultCode.REFERRAL, response.getResultCode());
        AssertJUnit.assertTrue(response.getReferralURLs().length > 0);
        for (String s : response.getReferralURLs()) {
          AssertJUnit.assertTrue(
            response.getReferralURLs()[0].startsWith(
              conn.getConnectionConfig().getLdapUrl()));
        }
      } catch (LdapException e) {
        AssertJUnit.assertEquals(ResultCode.REFERRAL, e.getResultCode());
        AssertJUnit.assertTrue(e.getReferralURLs().length > 0);
        for (String s : e.getReferralURLs()) {
          AssertJUnit.assertTrue(
            e.getReferralURLs()[0].startsWith(
              conn.getConnectionConfig().getLdapUrl()));
        }
      }
    } finally {
      conn.close();
    }

    try {
      conn.open();
      final CompareOperation compare = new CompareOperation(conn);
      try {
        final CompareRequest request = new CompareRequest(
          referralDn, new LdapAttribute(attrName, attrValue));
        request.setFollowReferrals(true);
        Response<Boolean> response = compare.execute(request);
        if (response.getResultCode() == ResultCode.COMPARE_TRUE) {
          AssertJUnit.assertTrue(response.getResult());
        } else {
          // some providers don't support authenticated referrals
          AssertJUnit.assertEquals(
            ResultCode.REFERRAL, response.getResultCode());
        }
      } catch (UnsupportedOperationException e) {
        // ignore this test if not supported
        AssertJUnit.assertNotNull(e);
      }
    } finally {
      conn.close();
    }
  }
}
