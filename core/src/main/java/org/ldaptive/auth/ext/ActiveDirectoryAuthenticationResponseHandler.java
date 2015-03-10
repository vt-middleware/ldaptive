/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth.ext;

import java.util.Calendar;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ad.io.FileTimeValueTranscoder;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;

/**
 * Attempts to parse the authentication response message and set the account state using data associated with active
 * directory.
 *
 * @author  Middleware Services
 */
public class ActiveDirectoryAuthenticationResponseHandler implements AuthenticationResponseHandler
{


  /** Maximum password age. */
  private long maxPasswordAge = -1;


  /** Default constructor. */
  public ActiveDirectoryAuthenticationResponseHandler() {}


  /**
   * Creates a new active directory authentication response handler.
   *
   * @param  passwordAge  length of time in milliseconds that a password is valid
   */
  public ActiveDirectoryAuthenticationResponseHandler(final long passwordAge)
  {
    if (passwordAge < 0) {
      throw new IllegalArgumentException("Password age must be >= 0");
    }
    maxPasswordAge = passwordAge;
  }


  @Override
  public void handle(final AuthenticationResponse response)
  {
    if (response.getResult()) {
      if (maxPasswordAge >= 0) {
        final LdapEntry entry = response.getLdapEntry();
        final LdapAttribute pwdLastSet = entry.getAttribute("pwdLastSet");
        if (pwdLastSet != null) {
          final Calendar exp = pwdLastSet.getValue(new FileTimeValueTranscoder());
          exp.setTimeInMillis(exp.getTimeInMillis() + maxPasswordAge);
          response.setAccountState(new ActiveDirectoryAccountState(exp));
        }
      }
    } else {
      if (response.getMessage() != null) {
        final ActiveDirectoryAccountState.Error adError = ActiveDirectoryAccountState.Error.parse(
          response.getMessage());
        if (adError != null) {
          response.setAccountState(new ActiveDirectoryAccountState(adError));
        }
      }
    }
  }
}
