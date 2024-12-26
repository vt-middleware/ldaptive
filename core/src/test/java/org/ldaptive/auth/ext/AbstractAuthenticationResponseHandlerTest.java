/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.ldaptive.BindResponse;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ResultCode;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationHandlerResponse;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.control.ResponseControl;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Base class for authentication response handler tests.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAuthenticationResponseHandlerTest
{

  /** clock used for testing. */
  protected final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());


  /**
   * Creates a new authentication response for testing.
   *
   * @param  authResultCode  authentication result code
   * @param  control  to add to the response
   * @param  attrs  attributes to add to the entry
   *
   * @return  authentication response
   */
  protected AuthenticationResponse createAuthenticationResponse(
    final AuthenticationResultCode authResultCode,
    final ResponseControl control,
    final LdapAttribute... attrs)
  {
    return new AuthenticationResponse(
      new AuthenticationHandlerResponse(
        BindResponse.builder()
          .resultCode(authResultCode == AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS ?
            ResultCode.SUCCESS : ResultCode.INVALID_CREDENTIALS)
          .controls(control)
          .build(),
        authResultCode,
        null),
      "cn=test",
      LdapEntry.builder().dn("cn=test").attributes(attrs).build());
  }


  /**
   * Creates a new authentication response for testing.
   *
   * @param  authResultCode  authentication result code
   * @param  diagnosticMessage  response diagnostic message
   * @param  attrs  attributes to add to the entry
   *
   * @return  authentication response
   */
  protected AuthenticationResponse createAuthenticationResponse(
    final AuthenticationResultCode authResultCode,
    final String diagnosticMessage,
    final LdapAttribute... attrs)
  {
    return new AuthenticationResponse(
      new AuthenticationHandlerResponse(
        BindResponse.builder()
          .resultCode(authResultCode == AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS ?
            ResultCode.SUCCESS : ResultCode.INVALID_CREDENTIALS)
          .diagnosticMessage(diagnosticMessage)
          .build(),
        authResultCode,
        null),
      "cn=test",
      LdapEntry.builder().dn("cn=test").attributes(attrs).build());
  }


  /**
   * Creates a new authentication response for testing.
   *
   * @param  resultCode  bind result code
   * @param  authResultCode  authentication result code
   * @param  diagnosticMessage  response diagnostic message
   * @param  attrs  attributes to add to the entry
   *
   * @return  authentication response
   */
  protected AuthenticationResponse createAuthenticationResponse(
    final ResultCode resultCode,
    final AuthenticationResultCode authResultCode,
    final String diagnosticMessage,
    final LdapAttribute... attrs)
  {
    return new AuthenticationResponse(
      new AuthenticationHandlerResponse(
        BindResponse.builder()
          .resultCode(resultCode)
          .diagnosticMessage(diagnosticMessage)
          .build(),
        authResultCode,
        null),
      "cn=test",
      LdapEntry.builder().dn("cn=test").attributes(attrs).build());
  }


  /**
   * Tests response handling.
   *
   * @param  handler  response handler to test
   * @param  response  to handle
   * @param  state  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "auth-ext", dataProvider = "responses")
  public void parse(
    final AuthenticationResponseHandler handler,
    final AuthenticationResponse response,
    final AccountState state)
    throws Exception
  {
    handler.handle(response);
    assertThat(response.getAccountState()).usingRecursiveComparison().isEqualTo(state);
  }
}
