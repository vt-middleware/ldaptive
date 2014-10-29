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
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.ldaptive.LdapUtils;

/**
 * Provides common implementation for SSL context initializer.
 *
 * @author  Middleware Services
 * @version  $Revision: 2940 $ $Date: 2014-03-31 11:10:46 -0400 (Mon, 31 Mar 2014) $
 */
public abstract class AbstractSSLContextInitializer
  implements SSLContextInitializer
{

  /** Trust managers. */
  private TrustManager[] trustManagers;


  /** {@inheritDoc} */
  @Override
  public TrustManager[] getTrustManagers()
    throws GeneralSecurityException
  {
    final TrustManager[] tm = createTrustManagers();
    TrustManager[] aggregate;
    if (tm == null) {
      aggregate = trustManagers != null ? aggregateTrustManagers(trustManagers)
                                        : null;
    } else {
      aggregate = aggregateTrustManagers(
        LdapUtils.concatArrays(tm, trustManagers));
    }
    return aggregate;
  }


  /**
   * Sets the trust managers.
   *
   * @param  managers  trust managers
   */
  @Override
  public void setTrustManagers(final TrustManager... managers)
  {
    trustManagers = managers;
  }


  /**
   * Creates any trust managers specific to this context initializer.
   *
   * @return  trust managers
   *
   * @throws  GeneralSecurityException  if an errors occurs while loading the
   * TrustManagers
   */
  protected abstract TrustManager[] createTrustManagers()
    throws GeneralSecurityException;


  /** {@inheritDoc} */
  @Override
  public SSLContext initSSLContext(final String protocol)
    throws GeneralSecurityException
  {
    final SSLContext ctx = SSLContext.getInstance(protocol);
    ctx.init(getKeyManagers(), getTrustManagers(), null);
    return ctx;
  }


  /**
   * Creates an {@link AggregateTrustManager} containing the supplied trust
   * managers.
   *
   * @param  managers  to aggregate
   *
   * @return  array containing a single aggregate trust manager
   */
  protected TrustManager[] aggregateTrustManagers(
    final TrustManager... managers)
  {
    X509TrustManager[] x509Managers = null;
    if (managers != null) {
      x509Managers = new X509TrustManager[managers.length];
      for (int i = 0; i < managers.length; i++) {
        x509Managers[i] = (X509TrustManager) managers[i];
      }
    }
    return new TrustManager[] {new AggregateTrustManager(x509Managers)};
  }
}
