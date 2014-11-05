/* See LICENSE for licensing and NOTICE for copyright. */
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
