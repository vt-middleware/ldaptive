/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.opendj;

import org.forgerock.opendj.ldap.Connection;
import org.forgerock.opendj.ldap.ErrorResultException;
import org.forgerock.opendj.ldap.LDAPConnectionFactory;
import org.forgerock.opendj.ldap.LDAPOptions;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Creates ldap connections using the OpenDJ LDAPConnectionFactory class.
 *
 * @author  Middleware Services
 */
public class OpenDJConnectionFactory extends AbstractProviderConnectionFactory<OpenDJProviderConfig>
{

  /** Ldap connection options. */
  private final LDAPOptions ldapOptions;


  /**
   * Creates a new OpenDJ connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  options  connection options
   */
  public OpenDJConnectionFactory(final String url, final OpenDJProviderConfig config, final LDAPOptions options)
  {
    super(url, config);
    ldapOptions = options;
  }


  @Override
  protected OpenDJConnection createInternal(final String url)
    throws LdapException
  {
    final LdapURL ldapUrl = new LdapURL(url);
    OpenDJConnection conn = null;
    boolean closeConn = false;
    try {
      final LDAPConnectionFactory cf = new LDAPConnectionFactory(
        ldapUrl.getLastEntry().getHostname(),
        ldapUrl.getLastEntry().getPort(),
        ldapOptions);
      final Connection c = cf.getConnection();
      conn = new OpenDJConnection(c, getProviderConfig());
    } catch (ErrorResultException e) {
      closeConn = true;
      throw new ConnectionException(e, org.ldaptive.ResultCode.valueOf(e.getResult().getResultCode().intValue()));
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
