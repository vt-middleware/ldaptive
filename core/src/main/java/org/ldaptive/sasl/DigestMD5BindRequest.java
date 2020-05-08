/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.Sasl;

/**
 * LDAP DIGEST-MD5 bind request.
 *
 * @author  Middleware Services
 */
public class DigestMD5BindRequest extends DefaultSaslClientRequest
{

  /** DIGEST-MD5 SASL mechanism. */
  public static final Mechanism MECHANISM = Mechanism.DIGEST_MD5;

  /** Quality of protection. */
  private final QualityOfProtection[] allowedQoP;

  /** Security strength. */
  private final SecurityStrength[] securityStrength;

  /** Authentication ID. */
  private final String authenticationID;

  /** Authorization ID. */
  private final String authorizationID;

  /** Realm. */
  private final String saslRealm;

  /** Password. */
  private final String password;

  /** Whether the server must authenticate to the client. */
  private final Boolean mutualAuthentication;


  /**
   * Creates a new DIGEST-MD5 bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password
   * @param  realm  SASL realm
   * @param  mutual  mutual authentication
   * @param  strength  security strength
   * @param  qop  quality of protection
   */
  public DigestMD5BindRequest(
    final String authID,
    final String authzID,
    final String pass,
    final String realm,
    final Boolean mutual,
    final SecurityStrength[] strength,
    final QualityOfProtection[] qop)
  {
    if (strength != null) {
      if (strength.length == 0) {
        throw new IllegalArgumentException("Security strength cannot be empty");
      } else {
        Stream.of(strength).forEach(s -> {
          if (s == null) {
            throw new IllegalArgumentException("Security strength cannot be null");
          }
        });
      }
    }
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
    mutualAuthentication = mutual != null ? mutual : Boolean.FALSE;
    securityStrength = strength != null ?
      strength : new SecurityStrength[] {SecurityStrength.HIGH, SecurityStrength.MEDIUM, SecurityStrength.LOW};
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
        if (saslRealm == null) {
          final String defaultRealm = rc.getDefaultText();
          if (defaultRealm == null) {
            throw new IllegalStateException("Default realm required, but none provided");
          } else {
            rc.setText(defaultRealm);
          }
        } else {
          rc.setText(saslRealm);
        }
      } else if (callback instanceof RealmChoiceCallback) {
        final RealmChoiceCallback rcc = (RealmChoiceCallback) callback;
        if (saslRealm == null) {
          throw new IllegalStateException(
            "Realm required, choose one of the following: " + Arrays.toString(rcc.getChoices()));
        } else if (rcc.getChoices() != null) {
          final int selectedIndex = IntStream.range(
            0, rcc.getChoices().length).filter(i -> rcc.getChoices()[i].equals(saslRealm)).findFirst().getAsInt();
          rcc.setSelectedIndex(selectedIndex);
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
    final Map<String, Object> props = new HashMap<>(3);
    props.put(
      Sasl.QOP,
      Stream.of(allowedQoP).map(QualityOfProtection::string).collect(Collectors.joining(",")));
    props.put(
      Sasl.STRENGTH,
      Stream.of(securityStrength).map(s -> s.name().toLowerCase()).collect(Collectors.joining(",")));
    props.put(Sasl.SERVER_AUTH, mutualAuthentication.toString());
    return Collections.unmodifiableMap(props);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("allowedQoP=").append(Arrays.toString(allowedQoP)).append(", ")
      .append("securityStrength=").append(Arrays.toString(securityStrength)).append(", ")
      .append("authenticationID=").append(authenticationID).append(", ")
      .append("authorizationID=").append(authorizationID).append(", ")
      .append("mutualAuthentication=").append(mutualAuthentication).append(", ")
      .append("saslRealm=").append(saslRealm).toString();
  }
}
