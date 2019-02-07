/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Contains all the configuration data for SASL Digest-MD5 authentication.
 *
 * @author  Middleware Services
 */
public class DigestMD5Config extends SaslConfig
{


  /** Default constructor. */
  public DigestMD5Config()
  {
    setMechanism(Mechanism.DIGEST_MD5);
  }
}
