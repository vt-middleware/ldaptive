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
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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
