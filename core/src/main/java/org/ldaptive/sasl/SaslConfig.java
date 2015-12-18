/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import org.ldaptive.AbstractConfig;

/**
 * Contains all the configuration data for SASL authentication.
 *
 * @author  Middleware Services
 */
public class SaslConfig extends AbstractConfig
{

  /** sasl mechanism. */
  private Mechanism mechanism;

  /** sasl authorization id. */
  private String authorizationId = "";

  /** perform mutual authentication. */
  private Boolean mutualAuthentication;

  /** sasl quality of protection. */
  private QualityOfProtection qualityOfProtection;

  /** sasl security strength. */
  private SecurityStrength securityStrength;

  /**
   * Copy constructor.
   * @param config Configuration to copy
   */
  public SaslConfig(final SaslConfig config)
  {
    setAuthorizationId(config.getAuthorizationId());
    setMutualAuthentication(config.getMutualAuthentication());
    setQualityOfProtection(config.getQualityOfProtection());
    setSecurityStrength(config.getSecurityStrength());
    setMechanism(config.getMechanism());
  }

  /** default constructor */
  public SaslConfig()
  {
  }


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
  public QualityOfProtection getQualityOfProtection()
  {
    return qualityOfProtection;
  }


  /**
   * Sets the sasl quality of protection.
   *
   * @param  qop  quality of protection
   */
  public void setQualityOfProtection(final QualityOfProtection qop)
  {
    checkImmutable();
    logger.trace("setting qualityOfProtection: {}", qop);
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


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::mechanism=%s, authorizationId=%s, mutualAuthentication=%s, " +
        "qualityOfProtection=%s, securityStrength=%s]",
        getClass().getName(),
        hashCode(),
        mechanism,
        authorizationId,
        mutualAuthentication,
        qualityOfProtection,
        securityStrength);
  }
}
