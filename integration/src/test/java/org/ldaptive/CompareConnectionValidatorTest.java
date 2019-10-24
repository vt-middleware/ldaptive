/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link CompareConnectionValidator}.
 *
 * @author  Middleware Services
 */
public class CompareConnectionValidatorTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = "validator")
  public void defaultSettings()
    throws Exception
  {
    final CompareConnectionValidator validator = new CompareConnectionValidator();
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    try {
      c.open();
      AssertJUnit.assertTrue(validator.apply(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(validator.apply(c));
  }


  /**
   * @param  compareDn  to test with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  @Parameters("ldapBindDn")
  public void customSettings(final String compareDn)
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    final CompareConnectionValidator validator1 = new CompareConnectionValidator(
      new CompareRequest(compareDn, "objectClass", "inetOrgPerson"));
    final CompareConnectionValidator validator2 = new CompareConnectionValidator(
      new CompareRequest("cn=does-not-exist", "objectClass", "inetOrgPerson"));
    try {
      c.open();
      AssertJUnit.assertTrue(validator1.apply(c));
      AssertJUnit.assertTrue(validator2.apply(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(validator1.apply(c));
    AssertJUnit.assertFalse(validator2.apply(c));
  }
}
