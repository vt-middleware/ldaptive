/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.testng.Assert;
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
    final CompareConnectionValidator validator = CompareConnectionValidator.builder()
      .timeout(Duration.ofSeconds(1))
      .build();
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    try (c) {
      c.open();
      Assert.assertTrue(validator.apply(c));
    }
    Assert.assertFalse(validator.apply(c));
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
    final CompareConnectionValidator validator1 = CompareConnectionValidator.builder()
      .request(new CompareRequest(compareDn, "objectClass", "inetOrgPerson"))
      .build();
    final CompareConnectionValidator validator2 = CompareConnectionValidator.builder()
      .request(new CompareRequest("cn=does-not-exist", "objectClass", "inetOrgPerson"))
      .build();
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    try (c) {
      c.open();
      Assert.assertTrue(validator1.apply(c));
      Assert.assertTrue(validator2.apply(c));
    }
    Assert.assertFalse(validator1.apply(c));
    Assert.assertFalse(validator2.apply(c));
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  public void applyAsync()
    throws Exception
  {
    final CompareConnectionValidator validator = CompareConnectionValidator.builder()
      .timeout(Duration.ofSeconds(1))
      .build();
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    final Connection c = cf.getConnection();
    try (c) {
      c.open();
      Assert.assertTrue(validator.applyAsync(c).get());
    }
    Assert.assertFalse(validator.applyAsync(c).get());
  }
}
