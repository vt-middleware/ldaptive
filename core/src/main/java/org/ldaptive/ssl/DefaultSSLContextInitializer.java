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
import java.security.KeyStore;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Provides a default implementation of SSL context initializer which allows the
 * setting of trust and key managers in order to create an SSL context.
 *
 * @author  Middleware Services
 * @version  $Revision: 2936 $ $Date: 2014-03-28 14:18:03 -0400 (Fri, 28 Mar 2014) $
 */
public class DefaultSSLContextInitializer extends AbstractSSLContextInitializer
{

  /** Key managers. */
  private KeyManager[] keyManagers;

  /** Whether default trust managers should be created. */
  private final boolean createDefaultTrustManagers;


  /**
   * Creates a new default ssl context initializer. Default trust managers will
   * be produced.
   */
  public DefaultSSLContextInitializer()
  {
    this(true);
  }


  /**
   * Creates a new default ssl context initializer.
   *
   * @param  defaultTrustManagers  whether default trust managers should be
   * created
   */
  public DefaultSSLContextInitializer(final boolean defaultTrustManagers)
  {
    createDefaultTrustManagers = defaultTrustManagers;
  }


  /** {@inheritDoc} */
  @Override
  protected TrustManager[] createTrustManagers()
    throws GeneralSecurityException
  {
    if (createDefaultTrustManagers) {
      final TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
      tmf.init((KeyStore) null);
      return tmf.getTrustManagers();
    }
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public KeyManager[] getKeyManagers()
    throws GeneralSecurityException
  {
    return keyManagers;
  }


  /**
   * Sets the key managers.
   *
   * @param  managers  key managers
   */
  public void setKeyManagers(final KeyManager... managers)
  {
    keyManagers = managers;
  }
}
