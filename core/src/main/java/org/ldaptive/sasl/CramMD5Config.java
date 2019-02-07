/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL Cram-MD5 authentication.
 *
 * @author  Middleware Services
 */
public class CramMD5Config extends SaslConfig
{


  /** Default constructor. */
  public CramMD5Config()
  {
    setMechanism(Mechanism.CRAM_MD5);
  }
}
