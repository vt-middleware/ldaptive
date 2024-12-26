/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.ldaptive.LdapAttribute;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link EDirectoryAuthenticationResponseHandler}.
 *
 * @author  Middleware Services
 */
public class EDirectoryAuthenticationResponseHandlerTest extends AbstractAuthenticationResponseHandlerTest
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
          createHandler(null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS, (String) null),
          null,
        },
        // auth failure no diagnostic message
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, (String) null),
          null,
        },
        // auth failure empty diagnostic message
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, ""),
          null,
        },
        // auth failure with unknown diagnostic message
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "unknown diagnostic message"),
          null,
        },
        // auth failure with account disabled diagnostic message
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
            "LDAP: error code 49 - NDS error: password expired (-223)"),
          new EDirectoryAccountState(EDirectoryAccountState.Error.PASSWORD_EXPIRED),
        },
        // auth success with no attributes found
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            ""),
          null,
        },
        // auth success with loginGraceRemaining, no passwordExpirationTime and no warning period
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder().name("loginGraceRemaining").values("3").build()),
          new EDirectoryAccountState(null, 3),
        },
        // auth success with passwordExpirationTime, no loginGraceRemaining and no warning period
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build()),
          new EDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(10),
            0),
        },
        // auth success with passwordExpirationTime and loginGraceRemaining, no warning period
        new Object[] {
          createHandler(null),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build(),
          LdapAttribute.builder().name("loginGraceRemaining").values("3").build()),
          new EDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(10),
            3),
        },
        // auth success with passwordExpirationTime and outside warning period, no loginGraceRemaining
        new Object[] {
          createHandler(Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build()),
          null,
        },
        // auth success with passwordExpirationTime and inside warning period, no loginGraceRemaining
        new Object[] {
          createHandler(Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(4)))
              .build()),
          new EDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(4),
            0),
        },
        // auth success with passwordExpirationTime, loginGraceRemaining and outside warning period
        new Object[] {
          createHandler(Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(10)))
              .build(),
          LdapAttribute.builder().name("loginGraceRemaining").values("3").build()),
          null,
        },
        // auth success with passwordExpirationTime, loginGraceRemaining and inside warning period
        new Object[] {
          createHandler(Period.ofDays(5)),
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            "",
            LdapAttribute.builder()
              .name("passwordExpirationTime")
              .values(new GeneralizedTimeValueTranscoder().encodeStringValue(ZonedDateTime.now(clock).plusDays(4)))
              .build(),
          LdapAttribute.builder().name("loginGraceRemaining").values("3").build()),
          new EDirectoryAccountState(
            ZonedDateTime.now(clock)
              .truncatedTo(ChronoUnit.MILLIS)
              .withZoneSameInstant(ZoneId.of("Z"))
              .plusDays(4),
            3),
        },
      };
  }


  /**
   * Creates a new edirectory authentication response handler.
   *
   * @param  warning  warning period
   *
   * @return  new edirectory authentication response handler
   */
  private EDirectoryAuthenticationResponseHandler createHandler(final Period warning)
  {
    final EDirectoryAuthenticationResponseHandler handler = new EDirectoryAuthenticationResponseHandler(clock);
    handler.setWarningPeriod(warning);
    return handler;
  }
}
