/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import org.ldaptive.transport.DefaultSaslClient;
import org.ldaptive.transport.GssApiSaslClient;

/**
 * LDAP GSSAPI bind request.
 *
 * @author  Middleware Services
 */
public class GssApiBindRequest extends DefaultSaslClientRequest
{

  /** GSSAPI SASL mechanism. */
  private static final Mechanism MECHANISM = Mechanism.GSSAPI;

  /** Authentication ID. */
  private final String authenticationID;

  /** Authorization ID. */
  private final String authorizationID;

  /** Realm. */
  private final String saslRealm;

  /** SASL client properties. */
  private final Map<String, ?> saslProperties;

  /** Password. */
  private final String password;

  /** Boolean that ensures the {@link GssApiSaslClient} is only returned on the first request. */
  private final AtomicBoolean invokeOnce = new AtomicBoolean();


  /**
   * Creates a new GSSAPI bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password to bind with
   * @param  realm  SASL realm
   * @param  props  SASL client properties
   */
  public GssApiBindRequest(
    final String authID,
    final String authzID,
    final String pass,
    final String realm,
    final Map<String, ?> props)
  {
    authenticationID = authID;
    authorizationID = authzID;
    password = pass;
    saslRealm = realm;
    saslProperties = Collections.unmodifiableMap(props);
  }


  @Override
  public SaslClient getSaslClient()
  {
    if (invokeOnce.compareAndSet(false, true)) {
      if (saslProperties.containsKey(GssApiSaslClient.JAAS_NAME_PROPERTY)) {
        return new GssApiSaslClient((String) saslProperties.get(GssApiSaslClient.JAAS_NAME_PROPERTY));
      } else {
        return new GssApiSaslClient();
      }
    } else {
      return new DefaultSaslClient();
    }
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
      } else if (callback instanceof RealmCallback) {
        final RealmCallback rc = (RealmCallback) callback;
        if (saslRealm == null) {
          throw new IllegalStateException("Realm required, but none provided");
        } else {
          rc.setText(saslRealm);
        }
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }


  @Override
  public Mechanism getMechanism()
  {
    return MECHANISM;
  }


  @Override
  public String getAuthorizationID()
  {
    return authorizationID;
  }


  @Override
  public Map<String, ?> getSaslProperties()
  {
    return saslProperties;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("authenticationID=").append(authenticationID).append(", ")
      .append("authorizationID=").append(authorizationID).append(", ")
      .append("realm=").append(saslRealm).append(", ")
      .append("saslProperties=").append(saslProperties).toString();
  }
}
