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

import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Hostname verifier that returns true for any hostname. Use with caution.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class AllowAnyHostnameVerifier
  implements HostnameVerifier, CertificateHostnameVerifier
{


  /** {@inheritDoc} */
  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public boolean verify(final String hostname, final X509Certificate cert)
  {
    return true;
  }
}
