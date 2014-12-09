/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.control.RequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for an LDAP authentication implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAuthenticationHandler
  implements AuthenticationHandler
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** controls used by this handler. */
  private RequestControl[] authenticationControls;


  /**
   * Returns the controls for this authentication handler.
   *
   * @return  controls
   */
  public RequestControl[] getAuthenticationControls()
  {
    return authenticationControls;
  }


  /**
   * Sets the controls for this authentication handler.
   *
   * @param  c  controls to set
   */
  public void setAuthenticationControls(final RequestControl... c)
  {
    authenticationControls = c;
  }


  @Override
  public AuthenticationHandlerResponse authenticate(
    final AuthenticationCriteria ac)
    throws LdapException
  {
    logger.debug("authenticate criteria={}", ac);

    AuthenticationHandlerResponse response = null;
    final Connection conn = getConnection();
    boolean closeConn = false;
    try {
      response = authenticateInternal(conn, ac);
    } catch (LdapException | RuntimeException e) {
      closeConn = true;
      throw e;
    } finally {
      if (closeConn) {
        conn.close();
      }
    }
    logger.debug("authenticate response={} for criteria={}", response, ac);
    return response;
  }


  /**
   * Returns a connection that the authentication operation should be performed
   * on.
   *
   * @return  connection
   *
   * @throws  LdapException  if an error occurs provisioning the connection
   */
  protected abstract Connection getConnection()
    throws LdapException;


  /**
   * Authenticate on the supplied connection using the supplied criteria.
   *
   * @param  c  to authenticate on
   * @param  criteria  criteria to authenticate with
   *
   * @return  authentication handler response
   *
   * @throws  LdapException  if the authentication fails
   */
  protected abstract AuthenticationHandlerResponse authenticateInternal(
    final Connection c,
    final AuthenticationCriteria criteria)
    throws LdapException;
}
