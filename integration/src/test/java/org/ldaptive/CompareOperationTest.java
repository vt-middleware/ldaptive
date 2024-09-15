/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.ldaptive.dn.Dn;
import org.ldaptive.referral.FollowCompareReferralHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
  @BeforeClass(groups = "compare")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = TestUtils.readFileIntoString(ldifFile);
    testLdapEntry = TestUtils.convertLdifToResult(ldif).getEntry();
    super.createLdapEntry(testLdapEntry);
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "compare")
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
    groups = "compare", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void compareTrue(final String dn, final String attrName, final String attrValue)
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(TestUtils.createConnectionFactory());
    final CompareResponse res = compare.execute(new CompareRequest(dn, attrName, attrValue));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.COMPARE_TRUE);
    assertThat(res.isTrue()).isTrue();
    assertThat(res.isFalse()).isFalse();
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
    groups = "compare", threadPoolSize = TEST_THREAD_POOL_SIZE, invocationCount = TEST_INVOCATION_COUNT,
    timeOut = TEST_TIME_OUT)
  public void compareFalse(final String dn, final String attrName, final String attrValue)
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(TestUtils.createConnectionFactory());
    final CompareResponse res = compare.execute(new CompareRequest(dn, "cn", "not-a-name"));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.COMPARE_FALSE);
    assertThat(res.isTrue()).isFalse();
    assertThat(res.isFalse()).isTrue();
  }


  /**
   * @param  dn  to compare.
   * @param  attrName  to compare with.
   * @param  attrValue  to compare with.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters({ "compareDn", "compareAttrName", "compareAttrValue" })
  @Test(groups = "compare")
  public void compareReferral(final String dn, final String attrName, final String attrValue)
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final CompareOperation compare = new CompareOperation(cf);
    final Dn origDn = new Dn(dn);
    final String referralDn =
      Dn.builder().add(origDn.subDn(0, 1)).add("ou=referrals").add(origDn.subDn(2)).build().format();
    CompareResponse res = compare.execute(new CompareRequest(referralDn, attrName, attrValue));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.REFERRAL);
    assertThat(res.isTrue()).isFalse();
    assertThat(res.isFalse()).isFalse();

    try {
      compare.setThrowCondition(result -> result.getResultCode() != ResultCode.COMPARE_TRUE);
      compare.execute(new CompareRequest(referralDn, attrName, attrValue));
    } catch (LdapException e) {
      assertThat(e.getResultCode()).isEqualTo(ResultCode.REFERRAL);
    } finally {
      compare.setThrowCondition(null);
    }

    compare.setReferralResultHandler(new FollowCompareReferralHandler(url -> {
      final ConnectionConfig refConfig = ConnectionConfig.copy(cf.getConnectionConfig());
      refConfig.setLdapUrl(url.replace("localhost", new LdapURL(cf.getConnectionConfig().getLdapUrl()).getHostname()));
      return new DefaultConnectionFactory(refConfig);
    }));
    res = compare.execute(new CompareRequest(referralDn, attrName, attrValue));
    assertThat(res.getResultCode()).isEqualTo(ResultCode.COMPARE_TRUE);
    assertThat(res.isTrue()).isTrue();
    assertThat(res.isFalse()).isFalse();
  }
}
