/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

/**
 * Maker interface for SASL mechanisms that use a custom client.
 *
 * @author  Middleware Services
 */
public interface SaslClientRequest
{


  /**
   * Returns the SASL client used by this request.
   *
   * @return  SASL client
   */
  SaslClient getSaslClient();
}
