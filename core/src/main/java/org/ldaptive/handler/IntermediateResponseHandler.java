/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import org.ldaptive.Connection;
import org.ldaptive.LdapException;
import org.ldaptive.Request;
import org.ldaptive.intermediate.IntermediateResponse;

/**
 * Provides handling of an ldap intermediate response.
 *
 * @author  Middleware Services
 */
public interface IntermediateResponseHandler extends Handler<Request, IntermediateResponse>
{


  @Override
  HandlerResult<IntermediateResponse> handle(Connection conn, Request request, IntermediateResponse response)
    throws LdapException;
}
