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

  /** Property for the JAAS entry name from a configuration file. */
  public static final String JAAS_NAME_PROPERTY = JAAS_OPTIONS_PROPERTY_PREFIX + "name";

  /** Default name of the JAAS configuration. */
  private static final String DEFAULT_GSSAPI_JAAS_NAME = "ldaptive-gssapi";

  /** Property for JAAS refreshConfig. */
  public static final String JAAS_REFRESH_CONFIG_PROPERTY = JAAS_OPTIONS_PROPERTY_PREFIX + "refreshConfig";

  /** Property for the login module class name for GSSAPI. */
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

  /** Name of the JAAS configuration. */
  private final String jaasName;

  /**
   * Whether to refresh the JAAS configuration prior to use.
   * See {@link javax.security.auth.login.Configuration#refresh()}.
   */
  private final boolean jaasRefreshConfig;

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
          Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue), Collections::unmodifiableMap));
    jaasLoginModule = (String) props.getOrDefault(JAAS_LOGIN_MODULE_PROPERTY, DEFAULT_GSSAPI_LOGIN_MODULE);
    jaasOptions = props.entrySet().stream()
      .filter(e ->
        e.getKey().startsWith(JAAS_OPTIONS_PROPERTY_PREFIX) &&
          !e.getKey().equals(JAAS_NAME_PROPERTY) &&
          !e.getKey().equals(JAAS_LOGIN_MODULE_PROPERTY))
      .collect(
        Collectors.collectingAndThen(
          Collectors.toMap(
            e -> e.getKey().substring(JAAS_OPTIONS_PROPERTY_PREFIX.length()),
            Map.Entry::getValue),
          Collections::unmodifiableMap));
    if (props.get(JAAS_NAME_PROPERTY) == null) {
      if (props.get(JAAS_LOGIN_MODULE_PROPERTY) == null && jaasOptions.isEmpty()) {
        jaasName = DEFAULT_GSSAPI_JAAS_NAME;
      } else {
        jaasName = null;
      }
    } else {
      jaasName = (String) props.get(JAAS_NAME_PROPERTY);
    }
    jaasRefreshConfig = Boolean.parseBoolean((String) props.getOrDefault(JAAS_REFRESH_CONFIG_PROPERTY, "false"));
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
   * Returns the entry name in a JAAS configuration file.
   *
   * @return  JAAS configuration name
   */
  public String getJaasName()
  {
    return jaasName;
  }


  /**
   * Returns whether to refresh the JAAS configuration prior to use. See {@link
   * javax.security.auth.login.Configuration#refresh()}.
   *
   * @return  whether to refresh the JAAS config
   */
  public boolean getJaasRefreshConfig()
  {
    return jaasRefreshConfig;
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
      .append("jaasName=").append(jaasName).append(", ")
      .append("jaasRefreshConfig=").append(jaasRefreshConfig).append(", ")
      .append("jaasLoginModule=").append(jaasLoginModule).append(", ")
      .append("jaasOptions=").append(jaasOptions).toString();
  }
}
