/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.unboundid;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Creates ldap connections using the UnboundID LDAPConnection class and
 * performs the startTLS extended operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2944 $ $Date: 2014-03-31 13:58:44 -0400 (Mon, 31 Mar 2014) $
 */
public class UnboundIDStartTLSConnectionFactory
  extends AbstractProviderConnectionFactory<UnboundIDProviderConfig>
{

  /** Socket factory to use for connections. */
  private final SocketFactory socketFactory;

  /** SSL context to use for startTLS. */
  private final SSLContext sslContext;

  /** UnboundID connection options. */
  private final LDAPConnectionOptions ldapOptions;


  /**
   * Creates a new Unbound ID connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  factory  SSL socket factory to use for LDAP and LDAPS
   * @param  sslCtx  SSL context to use for startTLS
   * @param  options  connection options
   */
  public UnboundIDStartTLSConnectionFactory(
    final String url,
    final UnboundIDProviderConfig config,
    final SocketFactory factory,
    final SSLContext sslCtx,
    final LDAPConnectionOptions options)
  {
    super(url, config);
    socketFactory = factory;
    sslContext = sslCtx;
    ldapOptions = options;
  }


  /** {@inheritDoc} */
  @Override
  protected UnboundIDConnection createInternal(final String url)
    throws LdapException
  {
    final LdapURL ldapUrl = new LdapURL(url);
    UnboundIDConnection conn = null;
    boolean closeConn = false;
    try {
      final LDAPConnection lc = new LDAPConnection(socketFactory, ldapOptions);
      conn = new UnboundIDConnection(lc, getProviderConfig());
      lc.connect(
        ldapUrl.getLastEntry().getHostname(),
        ldapUrl.getLastEntry().getPort());

      final ExtendedResult result = lc.processExtendedOperation(
        new StartTLSExtendedRequest(sslContext));
      if (result.getResultCode() != ResultCode.SUCCESS) {
        closeConn = true;
        throw new ConnectionException(
          "StartTLS failed",
          org.ldaptive.ResultCode.valueOf(result.getResultCode().intValue()));
      }
    } catch (LDAPException e) {
      closeConn = true;
      throw new ConnectionException(
        e,
        org.ldaptive.ResultCode.valueOf(e.getResultCode().intValue()));
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
