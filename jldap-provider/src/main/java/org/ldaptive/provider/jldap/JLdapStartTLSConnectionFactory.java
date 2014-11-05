/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import javax.net.ssl.SSLSocketFactory;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSEStartTLSFactory;

/**
 * Creates ldap connections using the JLDAP LDAPConnection class with the
 * startTLS extended operation.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class JLdapStartTLSConnectionFactory
  extends AbstractJLdapConnectionFactory<JLdapStartTLSConnection>
{

  /** SSL socket factory to use for startTLS. */
  private final SSLSocketFactory sslSocketFactory;


  /**
   * Creates a new jldap tls connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  constraints  connection constraints
   * @param  timeOut  time in milliseconds that operations will wait
   * @param  factory  SSL socket factory
   */
  public JLdapStartTLSConnectionFactory(
    final String url,
    final JLdapProviderConfig config,
    final LDAPConstraints constraints,
    final int timeOut,
    final SSLSocketFactory factory)
  {
    super(url, config, constraints, timeOut);
    sslSocketFactory = factory;
  }


  /** {@inheritDoc} */
  @Override
  protected LDAPConnection createLDAPConnection()
    throws LDAPException
  {
    LDAPConnection conn;
    if (sslSocketFactory != null) {
      conn = new LDAPConnection(new LDAPJSSEStartTLSFactory(sslSocketFactory));
    } else {
      conn = new LDAPConnection(new LDAPJSSEStartTLSFactory());
    }
    return conn;
  }


  /** {@inheritDoc} */
  @Override
  protected void initializeConnection(final LDAPConnection conn)
    throws LDAPException
  {
    conn.startTLS();
  }


  /** {@inheritDoc} */
  @Override
  protected JLdapStartTLSConnection createJLdapConnection(
    final LDAPConnection conn,
    final JLdapProviderConfig config)
  {
    return new JLdapStartTLSConnection(conn, config);
  }
}
