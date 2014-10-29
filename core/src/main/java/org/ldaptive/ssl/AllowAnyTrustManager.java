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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * Trust manager that trusts any certificate. Use with caution.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AllowAnyTrustManager implements X509TrustManager
{


  /** {@inheritDoc} */
  @Override
  public void checkClientTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException {}


  /** {@inheritDoc} */
  @Override
  public void checkServerTrusted(
    final X509Certificate[] chain,
    final String authType)
    throws CertificateException {}


  /** {@inheritDoc} */
  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }
}
