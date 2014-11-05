/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.async.handler;

import org.ldaptive.Connection;
import org.ldaptive.Request;
import org.ldaptive.handler.Handler;
import org.ldaptive.handler.HandlerResult;

/**
 * Provides post search handling of an exception thrown by an async operation.
 *
 * @author  Middleware Services
 */
public interface ExceptionHandler extends Handler<Request, Exception>
{


  /** {@inheritDoc} */
  @Override
  HandlerResult<Exception> handle(
    Connection conn,
    Request request,
    Exception exception);
}
