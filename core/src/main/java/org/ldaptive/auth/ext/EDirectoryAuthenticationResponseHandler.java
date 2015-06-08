/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.io.GeneralizedTimeValueTranscoder;

/**
 * Attempts to parse the authentication response and set the account state using data associated with eDirectory. The
 * {@link org.ldaptive.auth.Authenticator} should be configured to return 'passwordExpirationTime' and
 * 'loginGraceRemaining' attributes so they can be consumed by this handler.
 *
 * @author  Middleware Services
 */
public class EDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getMessage() != null) {
      final EDirectoryAccountState.Error edError = EDirectoryAccountState.Error.parse(response.getMessage());
      if (edError != null) {
        response.setAccountState(new EDirectoryAccountState(edError));
      }
    } else if (response.getResult()) {
      final LdapEntry entry = response.getLdapEntry();
      final LdapAttribute expTime = entry.getAttribute("passwordExpirationTime");
      final LdapAttribute loginRemaining = entry.getAttribute("loginGraceRemaining");
      Calendar exp = null;
      if (expTime != null) {
        exp = expTime.getValue(new GeneralizedTimeValueTranscoder());
      }
      if (exp != null || loginRemaining != null) {
        response.setAccountState(
          new EDirectoryAccountState(
            exp,
            loginRemaining != null ? Integer.parseInt(loginRemaining.getStringValue()) : 0));
      }
    }
  }
}
