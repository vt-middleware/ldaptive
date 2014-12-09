/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.async.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.handler.Handler;
import org.ldaptive.handler.HandlerResult;

/**
 * Provides post search handling of an ldap async request.
 *
 * @author  Middleware Services
 */
public interface AsyncRequestHandler extends Handler<Request, AsyncRequest>
{


  @Override
  HandlerResult<AsyncRequest> handle(
    Connection conn,
    Request request,
    AsyncRequest asyncRequest)
    throws LdapException;
}
