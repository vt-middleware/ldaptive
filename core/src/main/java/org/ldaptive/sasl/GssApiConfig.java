/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL GSSAPI authentication.
 *
 * @author  Middleware Services
 */
public class GssApiConfig extends SaslConfig
{

  /** sasl realm. */
  private String saslRealm;


  /** Default constructor. */
  public GssApiConfig()
  {
    super();
    setMechanism(Mechanism.GSSAPI);
  }

  /**
   * Copy constructor.
   * @param config Configuration to copy
   */
  public GssApiConfig(final GssApiConfig config)
  {
    super(config);
    setRealm(((GssApiConfig) config).getRealm());
    setMechanism(Mechanism.GSSAPI);
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
   * @param  realm  realm
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
    return
      String.format(
        "[%s@%d::mechanism=%s, authorizationId=%s, mutualAuthentication=%s, " +
        "qualityOfProtection=%s, securityStrength=%s, realm=%s]",
        getClass().getName(),
        hashCode(),
        getMechanism(),
        getAuthorizationId(),
        getMutualAuthentication(),
        getQualityOfProtection(),
        getSecurityStrength(),
        saslRealm);
  }
}
