/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.SimpleBindRequest;

/**
 * Provides an LDAP authentication implementation that leverages the LDAP bind operation.
 *
 * @author  Middleware Services
 */
public class SimpleBindAuthenticationHandler extends AbstractAuthenticationHandler
{


  /** Default constructor. */
  public SimpleBindAuthenticationHandler() {}


  /**
   * Creates a new simple bind authentication handler.
   *
   * @param  cf  connection factory
   */
  public SimpleBindAuthenticationHandler(final ConnectionFactory cf)
  {
    setConnectionFactory(cf);
  }


  @Override
  protected AuthenticationHandlerResponse authenticateInternal(
    final Connection c,
    final AuthenticationCriteria criteria)
    throws LdapException
  {
    final SimpleBindRequest request = new SimpleBindRequest(criteria.getDn(), criteria.getCredential().getString());
    request.setControls(processRequestControls(criteria));
    final Result bindResult = c.operation(request).execute();
    return new AuthenticationHandlerResponse(
      bindResult,
      bindResult.isSuccess() ?
        AuthenticationResultCode.AUTHENTICATION_HANDLER_SUCCESS :
        AuthenticationResultCode.AUTHENTICATION_HANDLER_FAILURE,
      c);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "factory=" + getConnectionFactory() + ", " +
      "controls=" + Arrays.toString(getAuthenticationControls()) + "]";
  }
}
