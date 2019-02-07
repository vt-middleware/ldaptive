/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.pool;

import org.ldaptive.AbstractTest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.ldaptive.TestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Test for {@link SearchValidator}.
 *
 * @author  Middleware Services
 */
public class SearchValidatorTest extends AbstractTest
{


  /** @throws  Exception  On test failure. */
  @Test(groups = "validator")
  public void defaultSettings()
    throws Exception
  {
    final SearchValidator validator = new SearchValidator();
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
    final SearchValidator validator1 = new SearchValidator(new SearchRequest(searchDn, new SearchFilter("(cn=*)")));
    final SearchValidator validator2 = new SearchValidator(new SearchRequest(searchDn, new SearchFilter("(dne=*)")));
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
