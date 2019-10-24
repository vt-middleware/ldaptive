/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link SearchConnectionValidator}.
 *
 * @author  Middleware Services
 */
public class SearchConnectionValidatorTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = "validator")
  public void defaultSettings()
    throws Exception
  {
    final SearchConnectionValidator validator = new SearchConnectionValidator();
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
   * @param  searchDn  to test with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  @Parameters("ldapBaseDn")
  public void customSettings(final String searchDn)
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    final SearchConnectionValidator validator1 = new SearchConnectionValidator(
      new SearchRequest(searchDn, new SearchFilter("(cn=*)")));
    final SearchConnectionValidator validator2 = new SearchConnectionValidator(
      new SearchRequest(searchDn, new SearchFilter("(dne=*)")));
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
