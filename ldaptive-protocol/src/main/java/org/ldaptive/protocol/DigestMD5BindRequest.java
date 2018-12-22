/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Arrays;
import java.util.Collections;
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
import org.ldaptive.sasl.QualityOfProtection;

/**
 * LDAP DIGEST-MD5 bind request.
 *
 * @author  Middleware Services
 */
public class DigestMD5BindRequest extends AbstractSASLClientRequest
{

  /** DIGEST-MD5 SASL mechanism name. */
  private static final String MECHANISM = "DIGEST-MD5";

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
   * Creates a new DIGEST-MD5 bind request.
   *
   * @param  authID  to bind as
   * @param  authzID  authorization ID
   * @param  pass  password
   * @param  r  realm
   * @param  qop  quality of protection
   */
  public DigestMD5BindRequest(
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
    allowedQoP = qop;
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
          final String defaultRealm = rc.getDefaultText();
          if (defaultRealm == null) {
            throw new IllegalStateException("Default realm required, but none provided");
          } else {
            rc.setText(defaultRealm);
          }
        } else {
          rc.setText(realm);
        }
      } else if (callback instanceof RealmChoiceCallback) {
        final RealmChoiceCallback rcc = (RealmChoiceCallback) callback;
        if (realm == null) {
          throw new IllegalStateException(
            "Realm required, choose one of the following: " + Arrays.toString(rcc.getChoices()));
        } else if (rcc.getChoices() != null) {
          final int selectedIndex = IntStream.range(
            0, rcc.getChoices().length).filter(i -> rcc.getChoices()[i].equals(realm)).findFirst().getAsInt();
          rcc.setSelectedIndex(selectedIndex);
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
    if (allowedQoP == null) {
      return null;
    }
    return Collections.singletonMap(
      Sasl.QOP,
      Stream.of(allowedQoP).map(q -> q.string()).collect(Collectors.joining(",")));
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
