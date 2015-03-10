/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider.jldap;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import org.ldaptive.LdapException;
import org.ldaptive.control.RequestControl;

/**
 * JLDAP provider implementation of ldap operations over TLS.
 *
 * @author  Middleware Services
 */
public class JLdapStartTLSConnection extends JLdapConnection
{

  /** Whether to call {@link LDAPConnection#stopTLS()} when {@link #close(RequestControl[])} is called. */
  private boolean stopTlsOnClose;


  /**
   * Creates a new jldap tls connection.
   *
   * @param  conn  ldap connection
   * @param  pc  provider configuration
   */
  public JLdapStartTLSConnection(final LDAPConnection conn, final JLdapProviderConfig pc)
  {
    super(conn, pc);
  }


  /**
   * Returns whether to call {@link LDAPConnection#stopTLS()} when {@link #close(RequestControl[])} is called.
   *
   * @return  stop TLS on close
   */
  public boolean getStopTlsOnClose()
  {
    return stopTlsOnClose;
  }


  /**
   * Sets whether to call {@link LDAPConnection#stopTLS()} when {@link #close(RequestControl[])} is called.
   *
   * @param  b  stop TLS on close
   */
  public void setStopTlsOnClose(final boolean b)
  {
    logger.trace("setting stopTlsOnClose: " + b);
    stopTlsOnClose = b;
  }


  @Override
  public void close(final RequestControl[] controls)
    throws LdapException
  {
    try {
      if (stopTlsOnClose) {
        getLDAPConnection().stopTLS();
      }
    } catch (LDAPException e) {
      logger.error("Error stopping TLS", e);
    } finally {
      super.close(controls);
    }
  }
}
