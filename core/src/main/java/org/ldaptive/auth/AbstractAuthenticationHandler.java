/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionFactoryManager;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.control.RequestControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for an LDAP authentication implementations.
 *
 * @author  Middleware Services
 */
public abstract class AbstractAuthenticationHandler implements AuthenticationHandler, ConnectionFactoryManager
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection factory. */
  private ConnectionFactory factory;

  /** controls used by this handler. */
  private RequestControl[] authenticationControls;


  @Override
  public ConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final ConnectionFactory cf)
  {
    factory = cf;
  }


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
   * @param  cntrls  controls to set
   */
  public void setAuthenticationControls(final RequestControl... cntrls)
  {
    authenticationControls = cntrls;
  }


  @Override
  public AuthenticationHandlerResponse authenticate(final AuthenticationCriteria ac)
    throws LdapException
  {
    logger.trace("authenticate criteria={}", ac);

    final AuthenticationHandlerResponse response;
    final Connection conn = factory.getConnection();
    boolean closeConn = false;
    try {
      conn.open();
      response = authenticateInternal(conn, ac);
    } catch (Exception e) {
      closeConn = true;
      throw e;
    } finally {
      if (closeConn) {
        conn.close();
      }
    }
    logger.debug("Authenticate response={} for criteria={}", response, ac);
    return response;
  }


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
  protected abstract AuthenticationHandlerResponse authenticateInternal(Connection c, AuthenticationCriteria criteria)
    throws LdapException;


  /**
   * Combines request controls in the {@link AuthenticationRequest} with {@link #authenticationControls}.
   *
   * @param  criteria  containing request controls
   *
   * @return  combined request controls or null
   */
  protected RequestControl[] processRequestControls(final AuthenticationCriteria criteria)
  {
    final RequestControl[] ctls;
    if (criteria.getAuthenticationRequest().getControls() != null) {
      if (getAuthenticationControls() != null) {
        ctls = LdapUtils.concatArrays(criteria.getAuthenticationRequest().getControls(), getAuthenticationControls());
      } else {
        ctls = criteria.getAuthenticationRequest().getControls();
      }
    } else {
      ctls = getAuthenticationControls();
    }
    return ctls;
  }
}
