/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link FreeIPAAuthenticationResponseHandler}.
 *
 * @author  Middleware Services
 */
public class FreeIPAAuthenticationResponseHandlerTest extends AbstractAuthenticationResponseHandlerTest
{


  /**
   * Password expiration test data.
   *
   * @return  error messages
   */
  @DataProvider(name = "responses")
  public Object[][] createTestParams()
  {
    return
      new Object[][] {
        // auth success no expiration
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS, (String) null),
          null,
        },
        // auth failure no diagnostic message
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, (String) null),
          new FreeIPAAccountState(FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND),
        },
        // auth failure empty diagnostic message
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, ""),
          new FreeIPAAccountState(FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND),
        },
        // auth failure with unknown diagnostic message
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "unknown diagnostic message"),
          new FreeIPAAccountState(FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND),
        },
        // auth failure with account disabled diagnostic message
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(
            ResultCode.UNWILLING_TO_PERFORM,
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "Account (Kerberos principal) is expired"),
          new FreeIPAAccountState(FreeIPAAccountState.Error.ACCOUNT_EXPIRED),
        },
        // auth success with no attributes found
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            ""),
          null,
        },
        // auth success with maxLoginFailures and no attribute, no expiration period and no warning period
        new Object[] {
          createHandler(null, null, 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            ""),
          null,
        },
        // auth success with maxLoginFailures and attribute, no expiration period and no warning period
        new Object[] {
          createHandler(null, null, 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build()),
          new FreeIPAAccountState(null, 2),
        },
        // auth success with expiration, no maxLoginFailures, no expiration period, no warning period
        new Object[] {
          createHandler(null, null, 0),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("krbPasswordExpiration")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(10),
            0),
        },
        // auth success with maxLoginFailures and expiration, no expiration period and no warning period
        new Object[] {
          createHandler(null, null, 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbPasswordExpiration")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(10),
            2),
        },
        // auth success with password change, no maxLoginFailures, expiration period, no warning period
        new Object[] {
          createHandler(Period.ofDays(30), null, 0),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("krbLastPwdChange")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(14)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(16),
            0),
        },
        // auth success with maxLoginFailures, password change, expiration period and no warning period
        new Object[] {
          createHandler(Period.ofDays(30), null, 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbLastPwdChange")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(14)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(16),
            2),
        },
        // auth success with maxLoginFailures, expiration, expiration period and warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5), 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbPasswordExpiration")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build()),
          null,
        },
        // auth success with maxLoginFailures, expiration, expiration period and warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5), 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbPasswordExpiration")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(4)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(4),
            2),
        },
        // auth success with maxLoginFailures, password change, expiration period and warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5), 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbLastPwdChange")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(14)))
              .build()),
          null,
        },
        // auth success with maxLoginFailures, password change, expiration period and warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5), 5),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("krbLoginFailedCount").values("3").build(),
            LdapAttribute.builder()
              .name("krbLastPwdChange")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(26)))
              .build()),
          new FreeIPAAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(4),
            2),
        },
      };
  }


  /**
   * Creates a new freeipa authentication response handler.
   *
   * @param  exp  expiration period
   * @param  warning  warning period
   * @param  loginFailures  max login failures
   *
   * @return  new freeipa authentication response handler
   */
  private FreeIPAAuthenticationResponseHandler createHandler(
    final Period exp, final Period warning, final int loginFailures)
  {
    final FreeIPAAuthenticationResponseHandler handler = new FreeIPAAuthenticationResponseHandler(clock);
    handler.setExpirationPeriod(exp);
    handler.setWarningPeriod(warning);
    handler.setMaxLoginFailures(loginFailures);
    return handler;
  }
}
