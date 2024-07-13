/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

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
      assertThat(validator.apply(c)).isTrue();
    }
    assertThat(validator.apply(c)).isFalse();
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
    final CompareConnectionValidator validator = CompareConnectionValidator.builder()
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
    CompareConnectionValidator validator = CompareConnectionValidator.builder()
      .validResultCodes(ResultCode.AUTH_METHOD_NOT_SUPPORTED)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isFalse();
    }

    validator = CompareConnectionValidator.builder()
      .validResultCodes(ResultCode.COMPARE_TRUE)
      .timeout(Duration.ofSeconds(5))
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isTrue();
    }

    validator = CompareConnectionValidator.builder()
      .validResultCodes(ResultCode.COMPARE_FALSE, ResultCode.COMPARE_TRUE)
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
    CompareConnectionValidator validator = CompareConnectionValidator.builder()
      .timeout(Duration.ofNanos(100))
      .timeoutIsFailure(false)
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isTrue();
    }

    validator = CompareConnectionValidator.builder()
      .timeout(Duration.ofNanos(100))
      .timeoutIsFailure(true)
      .build();
    try (Connection c = cf.getConnection()) {
      c.open();
      assertThat(validator.apply(c)).isFalse();
    }
  }
}
