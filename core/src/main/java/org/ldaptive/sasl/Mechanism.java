/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Enum to define SASL mechanisms.
 *
 * @author  Middleware Services
 */
public enum Mechanism {

  /** External authentication type. */
  EXTERNAL("EXTERNAL"),

  /** Digest MD5 authentication type. */
  DIGEST_MD5("DIGEST-MD5"),

  /** Cram MD5 authentication type. */
  CRAM_MD5("CRAM-MD5"),

  /** Kerberos authentication type. */
  GSSAPI("GSSAPI"),

  /** SCRAM SHA1. */
  SCRAM_SHA_1("SCRAM-SHA-1", "SHA-1", "HmacSHA1"),

  /** SCRAM SHA256. */
  SCRAM_SHA_256("SCRAM-SHA-256", "SHA-256", "HmacSHA256"),

  /** SCRAM SHA512. */
  SCRAM_SHA_512("SCRAM-SHA-512", "SHA-512", "HmacSHA512");


  /** SASL mechanism name. */
  private final String mechanismName;

  /** Digest algorithm name. */
  private final String[] properties;


  /**
   * Creates a new mechanism.
   *
   * @param  mechanism  SASL mechanism name
   */
  Mechanism(final String mechanism)
  {
    this(mechanism, (String[]) null);
  }


  /**
   * Creates a new mechanism.
   *
   * @param  mechanism  SASL mechanism name
   * @param  props  mechanism properties
   */
  Mechanism(final String mechanism, final String... props)
  {
    mechanismName = mechanism;
    properties = props;
  }


  /**
   * Returns the name of this mechanism.
   *
   * @return  mechanism name
   */
  public String mechanism()
  {
    return mechanismName;
  }


  /**
   * Returns any properties associated with this mechanism.
   *
   * @return  mechanism properties or null
   */
  public String[] properties()
  {
    return properties;
  }
}
