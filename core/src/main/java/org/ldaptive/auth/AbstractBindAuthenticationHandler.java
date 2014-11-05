/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.sasl.SaslConfig;

/**
 * Provides implementation common to bind authentication handlers.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
