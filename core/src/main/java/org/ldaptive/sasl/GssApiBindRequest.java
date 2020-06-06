/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
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

  /** SASL property to control the JAAS configuration name. */
  private static final String JAAS_OPTIONS_PROPERTY_PREFIX = "org.ldaptive.sasl.gssapi.jaas.";

  /** Login module class name for GSSAPI. */
  private static final String JAAS_LOGIN_MODULE_PROPERTY = JAAS_OPTIONS_PROPERTY_PREFIX + "loginModule";

  /** Default login module for GSSAPI. */
  private static final String DEFAULT_GSSAPI_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule";

  /** Authentication ID. */
  private final String authenticationID;

  /** Authorization ID. */
  private final String authorizationID;

  /** Realm. */
  private final String saslRealm;

  /** SASL client properties. */
  private final Map<String, ?> saslProperties;

  /** Class name of the JAAS login module to use for GSSAPI. */
  private final String jaasLoginModule;

  /** Options set on the JAAS login module. */
  private final Map<String, ?> jaasOptions;

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
    final Map<String, Object> props)
  {
    authenticationID = authID;
    authorizationID = authzID;
    password = pass;
    saslRealm = realm;
    saslProperties = props.entrySet().stream()
      .filter(e -> !e.getKey().startsWith(JAAS_OPTIONS_PROPERTY_PREFIX))
      .collect(
        Collectors.collectingAndThen(
          Collectors.toMap(e -> e.getKey(), e -> e.getValue()), Collections::unmodifiableMap));
    jaasLoginModule = (String) props.getOrDefault(JAAS_LOGIN_MODULE_PROPERTY, DEFAULT_GSSAPI_LOGIN_MODULE);
    jaasOptions = props.entrySet().stream()
      .filter(e ->
        e.getKey().startsWith(JAAS_OPTIONS_PROPERTY_PREFIX) && !e.getKey().equals(JAAS_LOGIN_MODULE_PROPERTY))
      .collect(
        Collectors.collectingAndThen(
          Collectors.toMap(
            e -> e.getKey().substring(JAAS_OPTIONS_PROPERTY_PREFIX.length()),
            e -> e.getValue()),
          Collections::unmodifiableMap));
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


  /**
   * Returns the class name of the JAAS login module.
   *
   * @return  JAAS login module class name
   */
  public String getJaasLoginModule()
  {
    return jaasLoginModule;
  }


  /**
   * Returns the JAAS options for the login module.
   *
   * @return  JAAS options
   */
  public Map<String, ?> getJaasOptions()
  {
    return jaasOptions;
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("authenticationID=").append(authenticationID).append(", ")
      .append("authorizationID=").append(authorizationID).append(", ")
      .append("realm=").append(saslRealm).append(", ")
      .append("saslProperties=").append(saslProperties).append(", ")
      .append("jaasLoginModule=").append(jaasLoginModule).append(", ")
      .append("jaasOptions=").append(jaasOptions).toString();
  }
}
