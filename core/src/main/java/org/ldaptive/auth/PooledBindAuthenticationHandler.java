/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.pool.PooledConnectionFactory;
import org.ldaptive.pool.PooledConnectionFactoryManager;

/**
 * Provides an LDAP authentication implementation that leverages a pool of LDAP
 * connections to perform the LDAP bind operation.
 *
 * @author  Middleware Services
 */
public class PooledBindAuthenticationHandler
  extends AbstractBindAuthenticationHandler
  implements PooledConnectionFactoryManager
{

  /** Connection factory. */
  private PooledConnectionFactory factory;


  /** Default constructor. */
  public PooledBindAuthenticationHandler() {}


  /**
   * Creates a new pooled bind authentication handler.
   *
   * @param  cf  connection factory
   */
  public PooledBindAuthenticationHandler(final PooledConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  @Override
  public PooledConnectionFactory getConnectionFactory()
  {
    return factory;
  }


  @Override
  public void setConnectionFactory(final PooledConnectionFactory cf)
  {
    factory = cf;
  }


  @Override
  protected Connection getConnection()
    throws LdapException
  {
    return factory.getConnection();
  }


  @Override
  protected AuthenticationHandlerResponse authenticateInternal(
    final Connection c,
    final AuthenticationCriteria criteria)
    throws LdapException
  {
    AuthenticationHandlerResponse response;
    final BindRequest request = new BindRequest(
      criteria.getDn(),
      criteria.getCredential());
    request.setSaslConfig(getAuthenticationSaslConfig());
    request.setControls(getAuthenticationControls());

    final BindOperation op = new BindOperation(c);
    try {
      final Response<Void> bindResponse = op.execute(request);
      response = new AuthenticationHandlerResponse(
        ResultCode.SUCCESS == bindResponse.getResultCode(),
        bindResponse.getResultCode(),
        c,
        bindResponse.getMessage(),
        bindResponse.getControls(),
        bindResponse.getMessageId());
    } catch (LdapException e) {
      if (ResultCode.INVALID_CREDENTIALS == e.getResultCode()) {
        response = new AuthenticationHandlerResponse(
          false,
          e.getResultCode(),
          c,
          e.getMessage(),
          e.getControls(),
          e.getMessageId());
      } else {
        throw e;
      }
    }
    return response;
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::factory=%s, saslConfig=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        factory,
        getAuthenticationSaslConfig(),
        Arrays.toString(getAuthenticationControls()));
  }
}
