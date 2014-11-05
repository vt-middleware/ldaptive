/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import javax.net.ssl.SSLSocketFactory;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;

/**
 * Creates LDAPS connections using the JLDAP LDAPConnection class.
 *
 * @author  Middleware Services
 */
public class JLdapSSLConnectionFactory
  extends AbstractJLdapConnectionFactory<JLdapSSLConnection>
{

  /** SSL socket factory to use for SSL. */
  private final SSLSocketFactory sslSocketFactory;


  /**
   * Creates a new jldap ssl connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  constraints  connection constraints
   * @param  timeOut  time in milliseconds that operations will wait
   * @param  factory  SSL socket factory
   */
  public JLdapSSLConnectionFactory(
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
      conn = new LDAPConnection(
        new LDAPJSSESecureSocketFactory(sslSocketFactory));
    } else {
      conn = new LDAPConnection(new LDAPJSSESecureSocketFactory());
    }
    return conn;
  }


  /** {@inheritDoc} */
  @Override
  protected JLdapSSLConnection createJLdapConnection(
    final LDAPConnection conn,
    final JLdapProviderConfig config)
  {
    return new JLdapSSLConnection(conn, config);
  }
}
