/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL EXTERNAL authentication.
 *
 * @author  Middleware Services
 */
public class ExternalConfig extends SaslConfig
{


  /** Default constructor. */
  public ExternalConfig()
  {
    setMechanism(Mechanism.EXTERNAL);
  }

  /**
   * Copy constructor.
   * @param config Configuration to copy
   */
  public ExternalConfig(final ExternalConfig config)
  {
    super(config);
    setMechanism(Mechanism.EXTERNAL);
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
        getMechanism(),
        getAuthorizationId(),
        getMutualAuthentication(),
        getQualityOfProtection(),
        getSecurityStrength());
  }
}
