/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;

/**
 * Provides handling of operation exceptions.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public interface OperationExceptionHandler<Q extends Request, S>
  extends Handler<Q, Response<S>>
{


  /** {@inheritDoc} */
  @Override
  HandlerResult<Response<S>> handle(
    Connection conn,
    Q request,
    Response<S> response)
    throws LdapException;
}
