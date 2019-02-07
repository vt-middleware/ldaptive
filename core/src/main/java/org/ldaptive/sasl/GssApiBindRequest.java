/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.Sasl;

/**
 * LDAP GSSAPI bind request.
 *
 * @author  Middleware Services
 */
public class GssApiBindRequest extends AbstractSaslClientRequest
{

  /** GSSAPI SASL mechanism name. */
  private static final String MECHANISM = "GSSAPI";

  /** Quality of protection. */
  private final QualityOfProtection[] allowedQoP;

  /** Authentication ID. */
  private final String authenticationID;

  /** Authorization ID. */
  private final String authorizationID;

  /** Realm. */
  private final String realm;

  /** Password. */
  private final String password;


  /**
   * Creates a new GSSAPI bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password to bind with
   * @param  r  realm
   * @param  qop  quality of protection
   */
  public GssApiBindRequest(
    final String authID,
    final String authzID,
    final String pass,
    final String r,
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
    realm = r;
    allowedQoP = qop != null ? qop : new QualityOfProtection[] {QualityOfProtection.AUTH};
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
        if (realm == null) {
          throw new IllegalStateException("Realm required, but none provided");
        } else {
          rc.setText(realm);
        }
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }


  @Override
  public String getMechanism()
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
    props.put(Sasl.SERVER_AUTH, "true");
    return props;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("allowedQoP=").append(Arrays.toString(allowedQoP)).append(", ")
      .append("authenticationID=").append(authenticationID).append(", ")
      .append("authorizationID=").append(authorizationID).append(", ")
      .append("realm=").append(realm).toString();
  }
}
