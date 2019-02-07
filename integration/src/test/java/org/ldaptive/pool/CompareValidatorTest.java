/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.AbstractTest;
import org.ldaptive.CompareRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link CompareValidator}.
 *
 * @author  Middleware Services
 */
public class CompareValidatorTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = "validator")
  public void defaultSettings()
    throws Exception
  {
    final CompareValidator validator = new CompareValidator();
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    try {
      c.open();
      AssertJUnit.assertTrue(validator.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(validator.validate(c));
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
    final CompareValidator validator1 = new CompareValidator(
      new CompareRequest(compareDn, "objectClass", "inetOrgPerson"));
    final CompareValidator validator2 = new CompareValidator(
      new CompareRequest("cn=does-not-exist", "objectClass", "inetOrgPerson"));
    try {
      c.open();
      AssertJUnit.assertTrue(validator1.validate(c));
      AssertJUnit.assertFalse(validator2.validate(c));
    } finally {
      c.close();
    }
    AssertJUnit.assertFalse(validator1.validate(c));
    AssertJUnit.assertFalse(validator2.validate(c));
  }
}
