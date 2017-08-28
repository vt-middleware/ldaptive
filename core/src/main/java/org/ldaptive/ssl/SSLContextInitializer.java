/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Provides an interface for the initialization of new SSL contexts.
 *
 * @author  Middleware Services
 */
public interface SSLContextInitializer
{


  /**
   * Creates an initialized SSLContext for the supplied protocol.
   *
   * @param  protocol  type to use for SSL
   *
   * @return  SSL context
   *
   * @throws  GeneralSecurityException  if the SSLContext cannot be created
   */
  SSLContext initSSLContext(String protocol)
    throws GeneralSecurityException;


  /**
   * Returns the trust managers used when creating SSL contexts.
   *
   * @return  trust managers
   *
   * @throws  GeneralSecurityException  if an errors occurs while loading the TrustManagers
   */
  TrustManager[] getTrustManagers()
    throws GeneralSecurityException;


  /**
   * Sets the trust managers. May be in isolation or in conjunction with other trust material.
   *
   * @param  managers  trust managers
   */
  void setTrustManagers(TrustManager... managers);


  /**
   * Returns the hostname verifier config used when created SSL contexts.
   *
   * @return  hostname verifier config
   */
  HostnameVerifierConfig getHostnameVerifierConfig();


  /**
   * Sets the hostname verifier config.
   *
   * @param  config  hostname verifier config
   */
  void setHostnameVerifierConfig(HostnameVerifierConfig config);


  /**
   * Returns the key managers used when creating SSL contexts.
   *
   * @return  key managers
   *
   * @throws  GeneralSecurityException  if an errors occurs while loading the KeyManagers
   */
  KeyManager[] getKeyManagers()
    throws GeneralSecurityException;
}
