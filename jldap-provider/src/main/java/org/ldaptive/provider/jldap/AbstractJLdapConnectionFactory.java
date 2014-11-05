/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPConstraints;
import com.novell.ldap.LDAPException;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.provider.AbstractProviderConnectionFactory;
import org.ldaptive.provider.ConnectionException;

/**
 * Base class for JLDAP connection factory implementations.
 *
 * @param  <T>  type of jldap connection
 *
 * @author  Middleware Services
 */
public abstract class AbstractJLdapConnectionFactory<T extends JLdapConnection>
  extends AbstractProviderConnectionFactory<JLdapProviderConfig>
{

  /** JLdap connection constraints. */
  private final LDAPConstraints ldapConstraints;

  /** Amount of time in milliseconds that operations will wait. */
  private final int socketTimeOut;


  /**
   * Creates a new abstract jldap connection factory.
   *
   * @param  url  of the ldap to connect to
   * @param  config  provider configuration
   * @param  constraints  connection constraints
   * @param  timeOut  time in milliseconds that operations will wait
   */
  public AbstractJLdapConnectionFactory(
    final String url,
    final JLdapProviderConfig config,
    final LDAPConstraints constraints,
    final int timeOut)
  {
    super(url, config);
    ldapConstraints = constraints;
    socketTimeOut = timeOut;
  }


  /** {@inheritDoc} */
  @Override
  protected T createInternal(final String url)
    throws LdapException
  {
    final LdapURL ldapUrl = new LdapURL(url);
    LDAPConnection conn = null;
    T jldapConn = null;
    boolean closeConn = false;
    try {
      conn = createLDAPConnection();
      if (ldapConstraints != null) {
        conn.setConstraints(ldapConstraints);
      }
      if (socketTimeOut > 0) {
        conn.setSocketTimeOut(socketTimeOut);
      }
      conn.connect(
        ldapUrl.getLastEntry().getHostnameWithPort(),
        LDAPConnection.DEFAULT_PORT);
      initializeConnection(conn);
      jldapConn = createJLdapConnection(conn, getProviderConfig());
    } catch (LDAPException e) {
      closeConn = true;
      throw new ConnectionException(e);
    } catch (RuntimeException e) {
      closeConn = true;
      throw e;
    } finally {
      if (closeConn) {
        try {
          if (conn != null) {
            conn.disconnect();
          }
        } catch (LDAPException e) {
          logger.debug("Problem tearing down connection", e);
        }
      }
    }
    return jldapConn;
  }


  /**
   * Creates an ldap connection for use with this connection factory.
   *
   * @return  ldap connection
   *
   * @throws  LDAPException  if an error occurs creating the connection
   */
  protected abstract LDAPConnection createLDAPConnection()
    throws LDAPException;


  /**
   * Initialize the supplied connection after a connection has been established.
   *
   * @param  conn  to initialize
   *
   * @throws  LDAPException  if an error occurs initializing the connection
   */
  protected void initializeConnection(final LDAPConnection conn)
    throws LDAPException {}


  /**
   * Creates a jldap connection of the appropriate type for this connection
   * factory.
   *
   * @param  conn  to create jldap connection with
   * @param  config  provider configuration
   *
   * @return  jldap connection
   */
  protected abstract T createJLdapConnection(
    final LDAPConnection conn,
    final JLdapProviderConfig config);
}
