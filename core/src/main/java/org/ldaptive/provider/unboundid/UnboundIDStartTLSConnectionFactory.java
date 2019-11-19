/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import javax.net.ssl.SSLSocketFactory;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import org.ldaptive.ConnectionStrategy;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Creates ldap connections using the UnboundID LDAPConnection class and performs the startTLS extended operation.
 *
 * @author  Middleware Services
 */
public class UnboundIDStartTLSConnectionFactory extends AbstractProviderConnectionFactory<UnboundIDProviderConfig>
{

  /** Socket factory to use for startTLS. */
  private final SSLSocketFactory socketFactory;

  /** UnboundID connection options. */
  private final LDAPConnectionOptions ldapOptions;


  /**
   * Creates a new Unbound ID connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  strategy  connection strategy
   * @param  config  provider configuration
   * @param  factory  SSL socket factory to use for startTLS
   * @param  options  connection options
   */
  public UnboundIDStartTLSConnectionFactory(
    final String url,
    final ConnectionStrategy strategy,
    final UnboundIDProviderConfig config,
    final SSLSocketFactory factory,
    final LDAPConnectionOptions options)
  {
    super(url, strategy, config);
    socketFactory = factory;
    ldapOptions = options;
  }


  @Override
  protected UnboundIDConnection createInternal(final String url)
    throws LdapException
  {
    final LdapURL ldapUrl = new LdapURL(url);
    UnboundIDConnection conn = null;
    boolean closeConn = false;
    try {
      final LDAPConnection lc = new LDAPConnection(getProviderConfig().getSocketFactory(), ldapOptions);
      conn = new UnboundIDConnection(lc, getProviderConfig());
      lc.connect(ldapUrl.getLastEntry().getHostname(), ldapUrl.getLastEntry().getPort());

      final ExtendedResult result = lc.processExtendedOperation(
        new StartTLSExtendedRequest(socketFactory != null ? socketFactory : getProviderConfig().getSSLSocketFactory()));
      if (result.getResultCode() != ResultCode.SUCCESS) {
        closeConn = true;
        throw new ConnectionException(
          "StartTLS failed",
          org.ldaptive.ResultCode.valueOf(result.getResultCode().intValue()));
      }
    } catch (LDAPException e) {
      closeConn = true;
      throw new ConnectionException(e, org.ldaptive.ResultCode.valueOf(e.getResultCode().intValue()));
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
