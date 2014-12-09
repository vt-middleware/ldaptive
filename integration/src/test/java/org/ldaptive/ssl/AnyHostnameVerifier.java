/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Returns true for any host it is asked to verify.
 *
 * @author  Middleware Services
 */
public class AnyHostnameVerifier implements HostnameVerifier
{


  @Override
  public boolean verify(final String hostname, final SSLSession session)
  {
    return true;
  }
}
