/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Enum to define SASL mechanisms.
 *
 * @author  Middleware Services
 */
public enum Mechanism {

  /** External authentication type. */
  EXTERNAL,

  /** Digest MD5 authentication type. */
  DIGEST_MD5,

  /** Cram MD5 authentication type. */
  CRAM_MD5,

  /** Kerberos authentication type. */
  GSSAPI
}
