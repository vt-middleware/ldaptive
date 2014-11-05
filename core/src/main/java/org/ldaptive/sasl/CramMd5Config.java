/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL Cram-MD5 authentication.
 *
 * @author  Middleware Services
 */
public class CramMd5Config extends SaslConfig
{


  /** Default constructor. */
  public CramMd5Config()
  {
    setMechanism(Mechanism.CRAM_MD5);
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::mechanism=%s, authorizationId=%s, mutualAuthentication=%s, " +
        "qualityOfProtection=%s, securityStrength=%s]",
        getClass().getName(),
        hashCode(),
        getMechanism(),
        getAuthorizationId(),
        getMutualAuthentication(),
        getQualityOfProtection(),
        getSecurityStrength());
  }
}
