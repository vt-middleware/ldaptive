/*
  $Id: NoHostnameVerifier.java 2210 2012-01-19 02:37:42Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2210 $
  Updated: $Date: 2012-01-18 21:37:42 -0500 (Wed, 18 Jan 2012) $
*/
package org.ldaptive.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Returns false for any host it is asked to verify.
 *
 * @author  Middleware Services
 * @version  $Revision: 2210 $ $Date: 2012-01-18 21:37:42 -0500 (Wed, 18 Jan 2012) $
 */
public class NoHostnameVerifier implements HostnameVerifier
{


  /** {@inheritDoc} */
  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    return false;
  }
}
