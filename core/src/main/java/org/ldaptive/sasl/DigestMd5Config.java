/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL Digest-MD5 authentication.
 *
 * @author  Middleware Services
 */
public class DigestMd5Config extends SaslConfig
{

  /** sasl realm. */
  private String saslRealm;


  /** Default constructor. */
  public DigestMd5Config()
  {
    setMechanism(Mechanism.DIGEST_MD5);
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


  /** {@inheritDoc} */
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
