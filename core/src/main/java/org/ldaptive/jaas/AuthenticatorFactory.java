/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jaas;

import java.util.Map;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.Authenticator;

/**
 * Provides an interface for creating authenticators needed by various JAAS modules.
 *
 * @author  Middleware Services
 */
public interface AuthenticatorFactory
{


  /**
   * Creates a new authenticator with the supplied JAAS options.
   *
   * @param  jaasOptions  JAAS configuration options
   *
   * @return  authenticator
   */
  Authenticator createAuthenticator(Map<String, ?> jaasOptions);


  /**
   * Creates a new authentication request with the supplied JAAS options.
   *
   * @param  jaasOptions  JAAS configuration options
   *
   * @return  authentication request
   */
  AuthenticationRequest createAuthenticationRequest(Map<String, ?> jaasOptions);
}
