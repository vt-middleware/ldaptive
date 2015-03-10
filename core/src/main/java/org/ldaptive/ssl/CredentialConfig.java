/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;

/**
 * Provides a base interface for all credential configurations. Since credential configs are invoked via reflection by
 * the PropertyInvoker their method signatures are not important. They only need to be able to create an SSL context
 * initializer once their properties have been set.
 *
 * @author  Middleware Services
 */
public interface CredentialConfig
{


  /**
   * Creates an SSL context initializer using the configured trust and authentication material in this config.
   *
   * @return  SSL context initializer
   *
   * @throws  GeneralSecurityException  if the ssl context initializer cannot be created
   */
  SSLContextInitializer createSSLContextInitializer()
    throws GeneralSecurityException;
}
