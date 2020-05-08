/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;
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

  /** Quality of protection. */
  private final QualityOfProtection[] allowedQoP;

  /** Authentication ID. */
  private final String authenticationID;

  /** Authorization ID. */
  private final String authorizationID;

  /** Realm. */
  private final String saslRealm;

  /** Password. */
  private final String password;

  /** Whether the server must authenticate the client. */
  private final Boolean mutualAuthentication;

  /** Boolean that ensures the {@link GssApiSaslClient} is only returned on the first request. */
  private final AtomicBoolean invokeOnce = new AtomicBoolean();


  /**
   * Creates a new GSSAPI bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password to bind with
   * @param  realm  SASL realm
   * @param  mutual  mutual authentication
   * @param  qop  quality of protection
   */
  public GssApiBindRequest(
    final String authID,
    final String authzID,
    final String pass,
    final String realm,
    final Boolean mutual,
    final QualityOfProtection... qop)
  {
    if (qop != null) {
      if (qop.length == 0) {
        throw new IllegalArgumentException("QOP cannot be empty");
      } else {
        Stream.of(qop).forEach(q -> {
          if (q == null) {
            throw new IllegalArgumentException("QOP cannot be null");
          }
        });
      }
    }
    authenticationID = authID;
    authorizationID = authzID;
    password = pass;
    saslRealm = realm;
    mutualAuthentication = mutual != null ? mutual : Boolean.TRUE;
    allowedQoP = qop != null ? qop : new QualityOfProtection[] {QualityOfProtection.AUTH};
  }


  @Override
  public SaslClient getSaslClient()
  {
    if (invokeOnce.compareAndSet(false, true)) {
      return new GssApiSaslClient();
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
      } else if (callback instanceof PasswordCallback) {
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
    final Map<String, Object> props = new HashMap<>(2);
    props.put(
      Sasl.QOP,
      Stream.of(allowedQoP).map(QualityOfProtection::string).collect(Collectors.joining(",")));
    props.put(Sasl.SERVER_AUTH, mutualAuthentication.toString());
    return props;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("allowedQoP=").append(Arrays.toString(allowedQoP)).append(", ")
      .append("authenticationID=").append(authenticationID).append(", ")
      .append("authorizationID=").append(authorizationID).append(", ")
      .append("mutualAuthentication=").append(mutualAuthentication).append(", ")
      .append("realm=").append(saslRealm).toString();
  }
}
