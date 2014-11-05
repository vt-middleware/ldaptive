/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.sasl.SaslConfig;

/**
 * Provides implementation common to bind authentication handlers.
 *
 * @author  Middleware Services
 */
public abstract class AbstractBindAuthenticationHandler
  extends AbstractAuthenticationHandler
{

  /** sasl configuration used by this handler. */
  private SaslConfig authenticationSaslConfig;


  /**
   * Returns the sasl config for this authentication handler.
   *
   * @return  sasl config
   */
  public SaslConfig getAuthenticationSaslConfig()
  {
    return authenticationSaslConfig;
  }


  /**
   * Sets the sasl config for this authentication handler.
   *
   * @param  config  sasl config
   */
  public void setAuthenticationSaslConfig(final SaslConfig config)
  {
    authenticationSaslConfig = config;
  }
}
