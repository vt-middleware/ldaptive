/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.ssl;

import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Provides an interface for the initialization of new SSL contexts.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
   * @throws  GeneralSecurityException  if an errors occurs while loading the
   * TrustManagers
   */
  TrustManager[] getTrustManagers()
    throws GeneralSecurityException;


  /**
   * Sets the trust managers. May be in isolation or in conjunction with other
   * trust material.
   *
   * @param  managers  trust managers
   */
  void setTrustManagers(TrustManager... managers);


  /**
   * Returns the key managers used when creating SSL contexts.
   *
   * @return  key managers
   *
   * @throws  GeneralSecurityException  if an errors occurs while loading the
   * KeyManagers
   */
  KeyManager[] getKeyManagers()
    throws GeneralSecurityException;
}
