/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.apache;

import java.time.Duration;
import org.apache.directory.api.ldap.model.exception.LdapOperationException;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.ResultCode;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Creates ldap connections using the Apache LdapNetworkConnection class.
 *
 * @author  Middleware Services
 */
public class ApacheLdapConnectionFactory extends AbstractProviderConnectionFactory<ApacheLdapProviderConfig>
{

  /** Connection configuration. */
  private final LdapConnectionConfig ldapConnectionConfig;

  /** Whether to startTLS on connections. */
  private final boolean useStartTLS;

  /** Timeout for responses. */
  private final Duration responseTimeOut;


  /**
   * Creates a new Apache LDAP connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  strategy  connection strategy
   * @param  config  provider configuration
   * @param  lcc  connection configuration
   * @param  tls  whether to startTLS on connections
   * @param  timeOut  timeout for responses
   */
  public ApacheLdapConnectionFactory(
    final String url,
    final ConnectionStrategy strategy,
    final ApacheLdapProviderConfig config,
    final LdapConnectionConfig lcc,
    final boolean tls,
    final Duration timeOut)
  {
    super(url, strategy, config);
    ldapConnectionConfig = lcc;
    useStartTLS = tls;
    responseTimeOut = timeOut;
  }


  @Override
  protected ApacheLdapConnection createInternal(final String url)
    throws LdapException
  {
    final LdapURL ldapUrl = new LdapURL(url);
    ldapConnectionConfig.setLdapHost(ldapUrl.getLastEntry().getHostname());
    ldapConnectionConfig.setLdapPort(ldapUrl.getLastEntry().getPort());

    ApacheLdapConnection conn = null;
    boolean closeConn = false;
    try {
      final LdapNetworkConnection lc = new LdapNetworkConnection(ldapConnectionConfig);
      conn = new ApacheLdapConnection(lc, getProviderConfig());
      lc.connect();
      if (useStartTLS) {
        lc.startTls();
      }
      if (responseTimeOut != null) {
        lc.setTimeOut(responseTimeOut.toMillis());
      }
    } catch (LdapOperationException e) {
      closeConn = true;
      throw new ConnectionException(e, ResultCode.valueOf(e.getResultCode().getValue()));
    } catch (org.apache.directory.api.ldap.model.exception.LdapException e) {
      closeConn = true;
      throw new ConnectionException(e);
    } catch (RuntimeException e) {
      closeConn = true;
      throw e;
    } finally {
      if (closeConn) {
        try {
          if (conn != null) {
            conn.close(null);
          }
        } catch (LdapException e) {
          logger.debug("Problem tearing down connection", e);
        }
      }
    }
    return conn;
  }
}
