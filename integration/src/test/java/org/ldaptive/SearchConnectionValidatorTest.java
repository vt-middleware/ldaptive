/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
      assertThat(validator.apply(c)).isTrue();
    }
    assertThat(validator.apply(c)).isFalse();
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
      assertThat(validator1.apply(c)).isTrue();
      assertThat(validator2.apply(c)).isTrue();
    }
    assertThat(validator1.apply(c)).isFalse();
    assertThat(validator2.apply(c)).isFalse();
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
      assertThat(validator.applyAsync(c).get()).isTrue();
    }
    assertThat(validator.applyAsync(c).get()).isFalse();
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
      assertThat(validator.apply(c)).isFalse();
    }

    validator = SearchConnectionValidator.builder()
      .validResultCodes(ResultCode.SUCCESS)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isTrue();
    }

    validator = SearchConnectionValidator.builder()
      .validResultCodes(ResultCode.PARTIAL_RESULTS, ResultCode.SUCCESS)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isTrue();
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
      assertThat(validator.apply(c)).isTrue();
    }

    validator = SearchConnectionValidator.builder()
      .timeout(Duration.ofNanos(100))
      .timeoutIsFailure(true)
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isFalse();
    }
  }
}
