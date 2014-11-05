/* See LICENSE for licensing and NOTICE for copyright. */
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
