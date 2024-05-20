/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;

/**
 * LDAP DIGEST-MD5 bind request.
 *
 * @author  Middleware Services
 */
public class DigestMD5BindRequest extends DefaultSaslClientRequest
{

  /** DIGEST-MD5 SASL mechanism. */
  public static final Mechanism MECHANISM = Mechanism.DIGEST_MD5;

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


  /**
   * Creates a new DIGEST-MD5 bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password
   * @param  realm  SASL realm
   * @param  props  SASL client properties
   */
  public DigestMD5BindRequest(
    final String authID,
    final String authzID,
    final String pass,
    final String realm,
    final Map<String, Object> props)
  {
    if (authID == null) {
      throw new IllegalArgumentException("Authentication ID cannot be null");
    }
    authenticationID = authID;
    authorizationID = authzID;
    if (pass == null) {
      throw new IllegalArgumentException("Password cannot be null");
    }
    password = pass;
    saslRealm = realm;
    saslProperties = props.entrySet().stream()
      .collect(
        Collectors.collectingAndThen(
          Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
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
  public String getAuthorizationID()
  {
    return authorizationID;
  }


  @Override
  public Map<String, ?> getSaslProperties()
  {
    return Collections.unmodifiableMap(saslProperties);
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      "authenticationID=" + authenticationID + ", " +
      "authorizationID=" + authorizationID + ", " +
      "saslRealm=" + saslRealm + ", " +
      "saslProperties=" + saslProperties;
  }
}
