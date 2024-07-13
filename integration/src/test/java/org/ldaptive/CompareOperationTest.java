/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

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
  public void compare(final String dn, final String attrName, final String attrValue)
    throws Exception
  {
    final CompareOperation compare = new CompareOperation(TestUtils.createConnectionFactory());
    final CompareResponse falseRes = compare.execute(new CompareRequest(dn, "cn", "not-a-name"));
    assertThat(falseRes.isTrue()).isFalse();
    assertThat(falseRes.isFalse()).isTrue();
    final CompareResponse trueRes = compare.execute(new CompareRequest(dn, attrName, attrValue));
    assertThat(trueRes.isTrue()).isTrue();
    assertThat(trueRes.isFalse()).isFalse();
  }
}
