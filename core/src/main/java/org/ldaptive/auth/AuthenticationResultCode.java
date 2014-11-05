/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

/**
 * Enum to define authentication results.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
