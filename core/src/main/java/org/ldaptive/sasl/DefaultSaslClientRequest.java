/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import org.ldaptive.control.RequestControl;
import org.ldaptive.transport.DefaultSaslClient;

/**
 * Base class for SASL client requests.
 *
 * @author  Middleware Services
 */
// CheckStyle:AbstractClassName OFF
public abstract class DefaultSaslClientRequest implements CallbackHandler
// CheckStyle:AbstractClassName ON
{

  /** LDAP controls. */
  private RequestControl[] controls;


  public RequestControl[] getControls()
  {
    return controls;
  }


  public void setControls(final RequestControl... cntrls)
  {
    controls = cntrls;
  }


  /**
   * Returns the SASL mechanism.
   *
   * @return  SASL mechanism
   */
  public abstract Mechanism getMechanism();


  /**
   * Returns the SASL authorization.
   *
   * @return  SASL authorization
   */
  public String getAuthorizationID()
  {
    return null;
  }


  /**
   * Returns the SASL properties.
   *
   * @return  SASL properties
   */
  public Map<String, ?> getSaslProperties()
  {
    return null;
  }


  /**
   * Returns the SASL client to use for this request.
   *
   * @return  SASL client
   */
  public SaslClient getSaslClient()
  {
    return new DefaultSaslClient();
  }


  /**
   * Creates SASL client properties from the supplied configuration.
   *
   * @param  config  SASL config
   *
   * @return  client properties
   */
  public static Map<String, Object> createProperties(final SaslConfig config)
  {
    final Map<String, Object> props = new HashMap<>();
    // add raw properties first, other properties will override if a conflict exists
    if (!config.getProperties().isEmpty()) {
      props.putAll(config.getProperties());
    }
    if (config.getQualityOfProtection() != null) {
      if (config.getQualityOfProtection().length == 0) {
        throw new IllegalArgumentException("QOP cannot be empty");
      }
      props.put(
        Sasl.QOP,
        Stream.of(config.getQualityOfProtection()).peek(q -> {
          if (q == null) {
            throw new IllegalArgumentException("QOP cannot be null");
          }
        }).map(QualityOfProtection::string).collect(Collectors.joining(",")));
    }
    if (config.getSecurityStrength() != null) {
      if (config.getSecurityStrength().length == 0) {
        throw new IllegalArgumentException("Security strength cannot be empty");
      }
      props.put(
        Sasl.STRENGTH,
        Stream.of(config.getSecurityStrength()).peek(s -> {
          if (s == null) {
            throw new IllegalArgumentException("Security strength cannot be null");
          }
        }).map(s -> s.name().toLowerCase()).collect(Collectors.joining(",")));
    }
    if (config.getMutualAuthentication() != null) {
      props.put(Sasl.SERVER_AUTH, config.getMutualAuthentication().toString());
    }
    return Collections.unmodifiableMap(props);
  }


  /**
   * Creates a new bind request for this client.
   *
   * @param  saslCredentials  to bind with
   *
   * @return  SASL bind request
   */
  public SaslBindRequest createBindRequest(final byte[] saslCredentials)
  {
    final SaslBindRequest req = new SaslBindRequest(getMechanism().mechanism(), saslCredentials);
    req.setControls(getControls());
    return req;
  }


  @Override
  public String toString()
  {
    return getClass().getName() + "@" + hashCode() + "::" + "controls=" + Arrays.toString(controls);
  }
}
