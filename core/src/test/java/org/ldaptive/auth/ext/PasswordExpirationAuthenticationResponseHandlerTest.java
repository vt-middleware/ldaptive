/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.time.ZonedDateTime;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.control.PasswordExpiredControl;
import org.ldaptive.control.PasswordExpiringControl;
import org.ldaptive.control.ResponseControl;
import org.testng.annotations.DataProvider;

/**
 * Unit test for {@link PasswordExpirationAuthenticationResponseHandler}.
 *
 * @author  Middleware Services
 */
public class PasswordExpirationAuthenticationResponseHandlerTest extends AbstractAuthenticationResponseHandlerTest
{

  /** handler to test. */
  private final PasswordExpirationAuthenticationResponseHandler handler =
    new PasswordExpirationAuthenticationResponseHandler(clock);


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
            new PasswordExpiredControl()),
          new PasswordExpirationAccountState(PasswordExpirationAccountState.Error.PASSWORD_EXPIRED),
        },
        new Object[] {
          handler,
          createAuthenticationResponse(
            AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS,
            new PasswordExpiringControl(expirationTimeSeconds)),
          new PasswordExpirationAccountState(exp),
        },
      };
  }
}
