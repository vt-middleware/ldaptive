/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.ldaptive.LdapAttribute;
import org.ldaptive.ad.transcode.FileTimeValueTranscoder;
import org.ldaptive.auth.AuthenticationResultCode;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link ActiveDirectoryAuthenticationResponseHandler}.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandlerTest extends AbstractAuthenticationResponseHandlerTest
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
          createHandler(null, null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS, (String) null),
          null,
        },
        // auth failure no diagnostic message
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, (String) null),
          null,
        },
        // auth failure empty diagnostic message
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, ""),
          null,
        },
        // auth failure with unknown diagnostic message
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "unknown diagnostic message"),
          null,
        },
        // auth failure with account disabled diagnostic message
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "80090308: LdapErr: DSID-0C09030B, comment: AcceptSecurityContext error, data 533, v893"),
          new ActiveDirectoryAccountState(ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED),
        },
        // auth success with no pwdLastSet attribute found
        new Object[] {
          createHandler(Period.ofDays(30), null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            ""),
          null,
        },
        // auth success with pwdLastSet, no expiration period and no warning period
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(10)))
              .build()),
          null,
        },
        // auth success with pwdLastSet, expiration period and no warning period
        new Object[] {
          createHandler(Period.ofDays(30), null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(10)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .minusDays(10)
              .plusDays(30)),
        },
        // auth success with pwdLastSet, expiration period inside of warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(26)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .minusDays(26)
              .plusDays(30)),
        },
        // auth success with pwdLastSet, expiration period outside of warning period
        new Object[] {
          createHandler(Period.ofDays(30), Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(24)))
              .build()),
          null,
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed, no expiration period, no warning period
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(13)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(13)),
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed, ignored expiration period, no warning period
        new Object[] {
          createHandler(Period.ofDays(30), null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(13)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(13)),
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed and pwdLastSet, no expiration period, no warning period
        new Object[] {
          createHandler(null, null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(24)))
              .build(),
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(13)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(13)),
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed and pwdLastSet
        // ignored expiration period, no warning period
        new Object[] {
          createHandler(Period.ofDays(30), null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("pwdLastSet")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).minusDays(24)))
              .build(),
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(13)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(13)),
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed, expiration period inside of warning period
        new Object[] {
          createHandler(null, Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(4)))
              .build()),
          new ActiveDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(4)),
        },
        // auth success with msDS-UserPasswordExpiryTimeComputed, expiration period outside of warning period
        new Object[] {
          createHandler(null, Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("msDS-UserPasswordExpiryTimeComputed")
              .values(new FileTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(13)))
              .build()),
          null,
        },
      };
  }


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  exp  expiration period
   * @param  warning  warning period
   *
   * @return  new active directory authentication response handler
   */
  private ActiveDirectoryAuthenticationResponseHandler createHandler(final Period exp, final Period warning)
  {
    final ActiveDirectoryAuthenticationResponseHandler handler =
      new ActiveDirectoryAuthenticationResponseHandler(clock);
    handler.setExpirationPeriod(exp);
    handler.setWarningPeriod(warning);
    return handler;
  }
}
