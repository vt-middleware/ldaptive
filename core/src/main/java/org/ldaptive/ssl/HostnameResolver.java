/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLSession;
import javax.net.ssl.StandardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves a hostname from an {@link SSLSession}.
 *
 * @author  Middleware Services
 */
public class HostnameResolver
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** SSL session. */
  private final SSLSession sslSession;


  /**
   * Creates a new hostname resolver.
   *
   * @param  session  SSL session
   */
  public HostnameResolver(final SSLSession session)
  {
    sslSession = session;
  }


  /**
   * Resolves a hostname from the SSL session.
   *
   * @return  hostname
   */
  public String resolve()
  {
    String hostname = null;
    if (sslSession instanceof ExtendedSSLSession) {
      final SNIServerName sniName = ((ExtendedSSLSession) sslSession).getRequestedServerNames().stream()
        .filter(n -> StandardConstants.SNI_HOST_NAME == n.getType())
        .findFirst()
        .orElse(null);
      if (sniName != null) {
        if (sniName instanceof SNIHostName) {
          hostname = ((SNIHostName) sniName).getAsciiName();
        } else {
          try {
            hostname = new SNIHostName(sniName.getEncoded()).getAsciiName();
          } catch (IllegalArgumentException e) {
            logger.warn("Illegal server name " + sniName, e);
          }
        }
      }
    }
    if (hostname == null && sslSession != null) {
      hostname = sslSession.getPeerHost();
    }
    return hostname;
  }
}
