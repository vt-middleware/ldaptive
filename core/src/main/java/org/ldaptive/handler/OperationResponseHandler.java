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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public interface OperationResponseHandler<Q extends Request, T>
  extends Handler<Q, Response<T>>
{


  /** {@inheritDoc} */
  @Override
  HandlerResult<Response<T>> handle(
    Connection conn,
    Q request,
    Response<T> response)
    throws LdapException;
}
