/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.ldaptive.AbstractConfig;
import org.ldaptive.LdapUtils;

/**
 * Contains basic configuration data for SASL authentication.
 *
 * @author  Middleware Services
 */
public final class SaslConfig extends AbstractConfig
{

  /** sasl properties. */
  private final Map<String, Object> properties = new HashMap<>();

  /** sasl mechanism. */
  private Mechanism mechanism;

  /** sasl authorization id. */
  private String authorizationId;

  /** perform mutual authentication. */
  private Boolean mutualAuthentication;

  /** sasl quality of protection. */
  private QualityOfProtection[] qualityOfProtection;

  /** sasl security strength. */
  private SecurityStrength[] securityStrength;

  /** sasl realm. */
  private String saslRealm;


  /**
   * Returns the sasl mechanism.
   *
   * @return  mechanism
   */
  public Mechanism getMechanism()
  {
    return mechanism;
  }


  /**
   * Sets the sasl mechanism.
   *
   * @param  m  mechanism
   */
  public void setMechanism(final Mechanism m)
  {
    assertMutable();
    logger.trace("setting mechanism: {}", m);
    mechanism = m;
  }


  /**
   * Returns the sasl authorization id.
   *
   * @return  authorization id
   */
  public String getAuthorizationId()
  {
    return authorizationId;
  }


  /**
   * Sets the sasl authorization id.
   *
   * @param  id  authorization id
   */
  public void setAuthorizationId(final String id)
  {
    assertMutable();
    logger.trace("setting authorizationId: {}", id);
    authorizationId = id;
  }


  /**
   * Returns whether mutual authentication should occur.
   *
   * @return  whether mutual authentication should occur
   */
  public Boolean getMutualAuthentication()
  {
    return mutualAuthentication;
  }


  /**
   * Sets whether mutual authentication should occur.
   *
   * @param  b  whether mutual authentication should occur
   */
  public void setMutualAuthentication(final Boolean b)
  {
    assertMutable();
    logger.trace("setting mutualAuthentication: {}", b);
    mutualAuthentication = b;
  }


  /**
   * Returns the sasl quality of protection.
   *
   * @return  quality of protection
   */
  public QualityOfProtection[] getQualityOfProtection()
  {
    return LdapUtils.copyArray(qualityOfProtection);
  }


  /**
   * Sets the sasl quality of protection.
   *
   * @param  qop  quality of protection
   */
  public void setQualityOfProtection(final QualityOfProtection... qop)
  {
    assertMutable();
    checkArrayContainsNull(qop);
    logger.trace("setting qualityOfProtection: {}", Arrays.toString(qop));
    qualityOfProtection = LdapUtils.copyArray(qop);
  }


  /**
   * Returns the sasl security strength.
   *
   * @return  security strength
   */
  public SecurityStrength[] getSecurityStrength()
  {
    return LdapUtils.copyArray(securityStrength);
  }


  /**
   * Sets the sasl security strength.
   *
   * @param  ss  security strength
   */
  public void setSecurityStrength(final SecurityStrength... ss)
  {
    assertMutable();
    checkArrayContainsNull(ss);
    logger.trace("setting securityStrength: {}", Arrays.toString(ss));
    securityStrength = LdapUtils.copyArray(ss);
  }


  /**
   * Returns the sasl realm.
   *
   * @return  realm
   */
  public String getRealm()
  {
    return saslRealm;
  }


  /**
   * Sets the sasl realm.
   *
   * @param  realm  to set
   */
  public void setRealm(final String realm)
  {
    assertMutable();
    logger.trace("setting realm: {}", realm);
    saslRealm = realm;
  }


  /**
   * Returns sasl properties.
   *
   * @return  properties
   */
  public Map<String, ?> getProperties()
  {
    return Collections.unmodifiableMap(properties);
  }


  /**
   * Sets sasl properties.
   *
   * @param  props  to set
   */
  public void setProperties(final Map<String, ?> props)
  {
    assertMutable();
    logger.trace("setting properties: {}", props);
    properties.putAll(props);
  }


  /**
   * Returns a sasl property.
   *
   * @param  name  of the property
   *
   * @return  property
   */
  public Object getProperty(final String name)
  {
    return properties.get(name);
  }


  /**
   * Sets a sasl property.
   *
   * @param  name  of the property
   * @param  value  of the property
   */
  public void setProperty(final String name, final Object value)
  {
    assertMutable();
    logger.trace("setting property: {}={}", name, value);
    properties.put(name, value);
  }


  @Override
  public String toString()
  {
    return "[" + getClass().getName() + "@" + hashCode() + "::" +
      "mechanism=" + mechanism + ", " +
      "authorizationId=" + authorizationId + ", " +
      "mutualAuthentication=" + mutualAuthentication + ", " +
      "qualityOfProtection=" + Arrays.toString(qualityOfProtection) + ", " +
      "securityStrength=" + Arrays.toString(securityStrength) + ", " +
      "realm=" + saslRealm + ", " +
      "properties=" + properties + "]";
  }


  /**
   * Returns a sasl config initialized with the supplied config.
   *
   * @param  config  sasl config to read properties from
   *
   * @return  sasl config
   */
  public static SaslConfig copy(final SaslConfig config)
  {
    final SaslConfig copy = new SaslConfig();
    copy.setMechanism(config.getMechanism());
    copy.setAuthorizationId(config.getAuthorizationId());
    copy.setMutualAuthentication(config.getMutualAuthentication());
    copy.setQualityOfProtection(config.getQualityOfProtection());
    copy.setSecurityStrength(config.getSecurityStrength());
    copy.setRealm(config.getRealm());
    copy.setProperties(new HashMap<>(config.getProperties()));
    return copy;
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static final class Builder
  {


    private final SaslConfig object = new SaslConfig();


    private Builder() {}


    public Builder makeImmutable()
    {
      object.freeze();
      return this;
    }


    public Builder mechanism(final Mechanism mechanism)
    {
      object.setMechanism(mechanism);
      return this;
    }


    public Builder authorizationId(final String id)
    {
      object.setAuthorizationId(id);
      return this;
    }


    public Builder mutualAuthentication(final Boolean b)
    {
      object.setMutualAuthentication(b);
      return this;
    }


    public Builder qualityOfProtection(final QualityOfProtection... protections)
    {
      object.setQualityOfProtection(protections);
      return this;
    }


    public Builder securityStrength(final SecurityStrength... strengths)
    {
      object.setSecurityStrength(strengths);
      return this;
    }


    public Builder realm(final String realm)
    {
      object.setRealm(realm);
      return this;
    }


    public Builder property(final String name, final Object value)
    {
      object.setProperty(name, value);
      return this;
    }


    public SaslConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
