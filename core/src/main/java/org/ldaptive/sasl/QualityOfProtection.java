/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Enum to define SASL quality of protection.
 *
 * @author  Middleware Services
 */
public enum QualityOfProtection {

  /** Authentication only. */
  AUTH,

  /** Authentication with integrity protection. */
  AUTH_INT,

  /** Authentication with integrity and privacy protection. */
  AUTH_CONF
}
