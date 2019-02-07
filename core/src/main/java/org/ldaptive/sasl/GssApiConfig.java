/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL GSSAPI authentication.
 *
 * @author  Middleware Services
 */
public class GssApiConfig extends SaslConfig
{


  /** Default constructor. */
  public GssApiConfig()
  {
    setMechanism(Mechanism.GSSAPI);
  }
}
