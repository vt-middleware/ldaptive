/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.testng.Assert;
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
    final SearchConnectionValidator validator = SearchConnectionValidator.builder()
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
   * @param  searchDn  to test with
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  @Parameters("ldapBaseDn")
  public void customSettings(final String searchDn)
    throws Exception
  {
    final SearchConnectionValidator validator1 = SearchConnectionValidator.builder()
      .request(new SearchRequest(searchDn, "(cn=*)"))
      .build();
    final SearchConnectionValidator validator2 = SearchConnectionValidator.builder()
      .request(new SearchRequest(searchDn, "(dne=*)"))
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
    final SearchConnectionValidator validator = SearchConnectionValidator.builder()
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


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  public void validResultCodes()
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    SearchConnectionValidator validator = SearchConnectionValidator.builder()
      .validResultCodes(ResultCode.AUTH_METHOD_NOT_SUPPORTED)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      Assert.assertFalse(validator.apply(c));
    }

    validator = SearchConnectionValidator.builder()
      .validResultCodes(ResultCode.SUCCESS)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      Assert.assertTrue(validator.apply(c));
    }

    validator = SearchConnectionValidator.builder()
      .validResultCodes(ResultCode.PARTIAL_RESULTS, ResultCode.SUCCESS)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      Assert.assertTrue(validator.apply(c));
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "validator")
  public void timeoutIsFailure()
    throws Exception
  {
    final ConnectionFactory cf = TestUtils.createConnectionFactory();
    SearchConnectionValidator validator = SearchConnectionValidator.builder()
      .timeout(Duration.ofNanos(100))
      .timeoutIsFailure(false)
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      Assert.assertTrue(validator.apply(c));
    }

    validator = SearchConnectionValidator.builder()
      .timeout(Duration.ofNanos(100))
      .timeoutIsFailure(true)
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      Assert.assertFalse(validator.apply(c));
    }
  }
}
