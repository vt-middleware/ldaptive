/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Returns false for any host it is asked to verify.
 *
 * @author  Middleware Services
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
