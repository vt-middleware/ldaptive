/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.Response;

/**
 * Provides handling of operation responses.
 *
 * @param  <Q>  type of ldap request
 * @param  <T>  type of ldap result contained in the response
 *
 * @author  Middleware Services
 */
public interface OperationResponseHandler<Q extends Request, T> extends Handler<Q, Response<T>>
{


  @Override
  HandlerResult<Response<T>> handle(Connection conn, Q request, Response<T> response)
    throws LdapException;
}
