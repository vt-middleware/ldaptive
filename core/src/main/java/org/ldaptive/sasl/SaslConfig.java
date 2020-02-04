/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.Arrays;
import org.ldaptive.AbstractConfig;

/**
 * Contains basic configuration data for SASL authentication.
 *
 * @author  Middleware Services
 */
public class SaslConfig extends AbstractConfig
{

  /** sasl mechanism. */
  private Mechanism mechanism;

  /** sasl authorization id. */
  private String authorizationId;

  /** perform mutual authentication. */
  private Boolean mutualAuthentication;

  /** sasl quality of protection. */
  private QualityOfProtection[] qualityOfProtection;

  /** sasl security strength. */
  private SecurityStrength securityStrength;

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
    checkImmutable();
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
    checkImmutable();
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
    checkImmutable();
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
    return qualityOfProtection;
  }


  /**
   * Sets the sasl quality of protection.
   *
   * @param  qop  quality of protection
   */
  public void setQualityOfProtection(final QualityOfProtection... qop)
  {
    checkImmutable();
    logger.trace("setting qualityOfProtection: {}", Arrays.toString(qop));
    qualityOfProtection = qop;
  }


  /**
   * Returns the sasl security strength.
   *
   * @return  security strength
   */
  public SecurityStrength getSecurityStrength()
  {
    return securityStrength;
  }


  /**
   * Sets the sasl security strength.
   *
   * @param  ss  security strength
   */
  public void setSecurityStrength(final SecurityStrength ss)
  {
    checkImmutable();
    logger.trace("setting securityStrength: {}", ss);
    securityStrength = ss;
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
    checkImmutable();
    logger.trace("setting realm: {}", realm);
    saslRealm = realm;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("mechanism=").append(mechanism).append(", ")
      .append("authorizationId=").append(authorizationId).append(", ")
      .append("mutualAuthentication=").append(mutualAuthentication).append(", ")
      .append("qualityOfProtection=").append(Arrays.toString(qualityOfProtection)).append(", ")
      .append("securityStrength=").append(securityStrength).append(", ")
      .append("realm=").append(saslRealm).append("]").toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static SaslConfig.Builder builder()
  {
    return new SaslConfig.Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final SaslConfig object = new SaslConfig();


    protected Builder() {}


    public SaslConfig.Builder mechanism(final Mechanism mechanism)
    {
      object.setMechanism(mechanism);
      return this;
    }


    public SaslConfig.Builder authorizationId(final String id)
    {
      object.setAuthorizationId(id);
      return this;
    }


    public SaslConfig.Builder mutualAuthentication(final Boolean b)
    {
      object.setMutualAuthentication(b);
      return this;
    }


    public SaslConfig.Builder qualityOfProtection(final QualityOfProtection... protections)
    {
      object.setQualityOfProtection(protections);
      return this;
    }


    public SaslConfig.Builder securityStrength(final SecurityStrength strength)
    {
      object.setSecurityStrength(strength);
      return this;
    }


    public SaslConfig.Builder realm(final String realm)
    {
      object.setRealm(realm);
      return this;
    }


    public SaslConfig build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
