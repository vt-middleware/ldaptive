/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

/**
 * Enum to define authentication results.
 *
 * @author  Middleware Services
 */
public enum AuthenticationResultCode {

  /** The configured authentication handler produced a result of true. */
  AUTHENTICATION_HANDLER_SUCCESS,

  /** The configured authentication handler produced a result of false. */
  AUTHENTICATION_HANDLER_FAILURE,

  /** The supplied credential was empty or null. */
  INVALID_CREDENTIAL,

  /** The configured DN resolver produced an empty or null value. */
  DN_RESOLUTION_FAILURE
}
