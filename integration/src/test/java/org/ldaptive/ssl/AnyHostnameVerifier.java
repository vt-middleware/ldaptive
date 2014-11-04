/*
  $Id: AnyHostnameVerifier.java 2226 2012-01-27 23:16:37Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2226 $
  Updated: $Date: 2012-01-27 18:16:37 -0500 (Fri, 27 Jan 2012) $
*/
package org.ldaptive.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Returns true for any host it is asked to verify.
 *
 * @author  Middleware Services
 * @version  $Revision: 2226 $ $Date: 2012-01-27 18:16:37 -0500 (Fri, 27 Jan 2012) $
 */
public class AnyHostnameVerifier implements HostnameVerifier
{


  /** {@inheritDoc} */
  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    return true;
  }
}
