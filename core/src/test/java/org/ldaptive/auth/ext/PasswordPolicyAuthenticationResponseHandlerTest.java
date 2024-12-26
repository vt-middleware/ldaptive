/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.control.PasswordPolicyControl;
import org.ldaptive.control.ResponseControl;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link PasswordPolicyAuthenticationResponseHandler}.
 *
 * @author  Middleware Services
 */
public class PasswordPolicyAuthenticationResponseHandlerTest extends AbstractAuthenticationResponseHandlerTest
{

  /** handler to test. */
  private final PasswordPolicyAuthenticationResponseHandler handler =
    new PasswordPolicyAuthenticationResponseHandler(clock);


  /**
   * Password expiration test data.
   *
   * @return  error messages
   */
  @DataProvider(name = "responses")
  public Object[][] createTestParams()
  {
    final int expirationTimeSeconds = 123456;
    final ZonedDateTime exp = ZonedDateTime.now(clock).plusSeconds(expirationTimeSeconds);
    return
      new Object[][] {
        new Object[] {
          handler,
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS, (ResponseControl) null),
          null,
        },
        new Object[] {
          handler,
          createAuthenticationResponse(AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE, (ResponseControl) null),
          null,
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordPolicyControl(PasswordPolicyControl.Error.PASSWORD_EXPIRED)),
          new PasswordPolicyAccountState(PasswordPolicyControl.Error.PASSWORD_EXPIRED),
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordPolicyControl(-1, 5)),
          new PasswordPolicyAccountState(5),
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordPolicyControl(-1, 5, PasswordPolicyControl.Error.PASSWORD_EXPIRED)),
          new PasswordPolicyAccountState(5, PasswordPolicyControl.Error.PASSWORD_EXPIRED),
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordPolicyControl(expirationTimeSeconds, -1)),
          new PasswordPolicyAccountState(exp),
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordPolicyControl(expirationTimeSeconds, -1, PasswordPolicyControl.Error.PASSWORD_EXPIRED)),
          new PasswordPolicyAccountState(exp, PasswordPolicyControl.Error.PASSWORD_EXPIRED),
        },
      };
  }
}
