/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.async;

import org.ldaptive.AbstractOperation;
import org.ldaptive.Connection;
import org.ldaptive.Request;
import org.ldaptive.async.handler.AsyncRequestHandler;
import org.ldaptive.async.handler.ExceptionHandler;

/**
 * Base class for asynchronous ldap operations.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public abstract class AbstractAsyncOperation<Q extends Request, S>
  extends AbstractOperation<Q, S>
{

  /** Handlers to handle async requests. */
  private AsyncRequestHandler[] asyncRequestHandlers;

  /** Handler to handle exceptions. */
  private ExceptionHandler exceptionHandler;


  /**
   * Creates a new abstract async operation.
   *
   * @param  conn  to use for this operation
   */
  public AbstractAsyncOperation(final Connection conn)
  {
    super(conn);
  }


  /**
   * Returns the async request handlers.
   *
   * @return  async request handlers
   */
  public AsyncRequestHandler[] getAsyncRequestHandlers()
  {
    return asyncRequestHandlers;
  }


  /**
   * Sets the async request handlers.
   *
   * @param  handlers  async request handlers
   */
  public void setAsyncRequestHandlers(final AsyncRequestHandler... handlers)
  {
    asyncRequestHandlers = handlers;
  }


  /**
   * Returns the exception handler.
   *
   * @return  exception handler
   */
  public ExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }


  /**
   * Sets the exception handler.
   *
   * @param  handler  exception handler
   */
  public void setExceptionHandler(final ExceptionHandler handler)
  {
    exceptionHandler = handler;
  }
}
