/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * LDAP CRAM-MD5 bind request.
 *
 * @author  Middleware Services
 */
public class CramMD5BindRequest extends DefaultSaslClientRequest
{

  /** CRAM-MD5 SASL mechanism. */
  public static final Mechanism MECHANISM = Mechanism.CRAM_MD5;

  /** Authentication ID. */
  private final String authenticationID;

  /** Password. */
  private final String password;


  /**
   * Creates a new CRAM-MD5 bind request.
   *
   * @param  authID  to bind as
   * @param  pass  password to bind with
   */
  public CramMD5BindRequest(final String authID, final String pass)
  {
    authenticationID = authID;
    password = pass;
  }


  @Override
  public void handle(final Callback[] callbacks)
    throws UnsupportedCallbackException
  {
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback) {
        ((NameCallback) callback).setName(authenticationID);
      } else if (callback instanceof PasswordCallback && password != null) {
        ((PasswordCallback) callback).setPassword(password.toCharArray());
      } else {
        throw new UnsupportedCallbackException(callback, "Unsupported callback: " + callback);
      }
    }
  }


  @Override
  public Mechanism getMechanism()
  {
    return MECHANISM;
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "authenticationID=" + authenticationID;
  }
}
