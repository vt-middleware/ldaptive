/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Enum to define SASL mechanisms.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
