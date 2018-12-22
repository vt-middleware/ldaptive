/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * Interface for SASL requests that use a SASL client.
 *
 * @author  Middleware Services
 */
public interface SASLClientRequest extends CallbackHandler
{


  /**
   * Returns the SASL mechanism.
   *
   * @return  SASL mechanism
   */
  String getMechanism();


  /**
   * Returns the SASL authorization.
   *
   * @return  SASL authorization
   */
  String getAuthorizationID();


  /**
   * Returns the SASL properties.
   *
   * @return  SASL properties
   */
  Map<String, ?> getSaslProperties();


  /**
   * Creates a new bind request for this client.
   *
   * @param  saslCredentials  to bind with
   *
   * @return  SASL bind request
   */
  SASLBindRequest createBindRequest(byte[] saslCredentials);
}
