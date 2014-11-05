/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;

/**
 * Returns a handler result containing the response passed to {@link
 * #handle(Connection, Request, Response)}.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class NoOpOperationExceptionHandler<Q extends Request, S>
  implements OperationExceptionHandler<Q, S>
{


  /** {@inheritDoc} */
  @Override
  public HandlerResult<Response<S>> handle(
    final Connection conn,
    final Q request,
    final Response<S> response)
    throws LdapException
  {
    return new HandlerResult<>(response);
  }
}
