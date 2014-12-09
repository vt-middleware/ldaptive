/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.handler.AbstractRetryOperationExceptionHandler;
import org.ldaptive.handler.Handler;
import org.ldaptive.handler.HandlerResult;
import org.ldaptive.handler.OperationExceptionHandler;
import org.ldaptive.handler.OperationResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common implementation for ldap operations.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperation<Q extends Request, S>
  implements Operation<Q, S>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Connection to perform operation. */
  private final Connection connection;

  /** Handler to handle operation exceptions. */
  private OperationExceptionHandler<Q, S> operationExceptionHandler =
    new ReopenOperationExceptionHandler();

  /** Handlers to handle operation responses. */
  private OperationResponseHandler<Q, S>[] operationResponseHandlers;


  /**
   * Creates a new abstract operation.
   *
   * @param  conn  to use for this operation
   */
  public AbstractOperation(final Connection conn)
  {
    connection = conn;
  }


  /**
   * Returns the connection used for this operation.
   *
   * @return  connection
   */
  protected Connection getConnection()
  {
    return connection;
  }


  /**
   * Returns the operation exception handler.
   *
   * @return  operation exception handler
   */
  public OperationExceptionHandler<Q, S> getOperationExceptionHandler()
  {
    return operationExceptionHandler;
  }


  /**
   * Sets the operation exception handler.
   *
   * @param  handler  operation exception handler
   */
  public void setOperationExceptionHandler(
    final OperationExceptionHandler<Q, S> handler)
  {
    operationExceptionHandler = handler;
  }


  /**
   * Returns the operation response handlers.
   *
   * @return  operation response handlers
   */
  public OperationResponseHandler<Q, S>[] getOperationResponseHandlers()
  {
    return operationResponseHandlers;
  }


  /**
   * Sets the operation response handlers.
   *
   * @param  handlers  operation response handlers
   */
  @SuppressWarnings("unchecked")
  public void setOperationResponseHandlers(
    final OperationResponseHandler<Q, S>... handlers)
  {
    operationResponseHandlers = handlers;
  }


  /**
   * Call the provider specific implementation of this ldap operation.
   *
   * @param  request  ldap request
   *
   * @return  ldap response
   *
   * @throws  LdapException  if the invocation fails
   */
  protected abstract Response<S> invoke(final Q request)
    throws LdapException;


  @Override
  public Response<S> execute(final Q request)
    throws LdapException
  {
    logger.debug("execute request={} with connection={}", request, connection);

    Response<S> response = null;
    try {
      response = invoke(request);
    } catch (OperationException e) {
      if (operationExceptionHandler == null) {
        throw e;
      }
      logger.debug(
        "Error performing LDAP operation, invoking exception handler: {}",
        operationExceptionHandler,
        e);

      final HandlerResult<Response<S>> hr = operationExceptionHandler.handle(
        connection,
        request,
        response);
      if (hr.getAbort()) {
        throw e;
      }
      response = hr.getResult();
    }

    // execute response handlers
    final HandlerResult<Response<S>> hr = executeHandlers(
      getOperationResponseHandlers(),
      request,
      response);

    logger.debug(
      "execute response={} for request={} with connection={}",
      new Object[] {hr.getResult(), request, connection});
    return hr.getResult();
  }


  /**
   * Processes each handler and returns a handler result containing a result
   * processed by all handlers. If any handler indicates that the operation
   * should be aborted, that flag is returned to the operation after all
   * handlers have been invoked.
   *
   * @param  <Q>  type of request
   * @param  <S>  type of response
   * @param  handlers  to invoke
   * @param  request  the operation was performed with
   * @param  result  from the operation
   *
   * @return  handler result
   *
   * @throws  LdapException  if an error occurs processing a handler
   */
  protected <Q extends Request, S> HandlerResult<S> executeHandlers(
    final Handler<Q, S>[] handlers,
    final Q request,
    final S result)
    throws LdapException
  {
    S processed = result;
    boolean abort = false;
    if (handlers != null && handlers.length > 0) {
      for (Handler<Q, S> handler : handlers) {
        if (handler != null) {
          try {
            final HandlerResult<S> hr = handler.handle(
              getConnection(),
              request,
              processed);
            if (hr != null) {
              if (hr.getAbort()) {
                abort = true;
              }
              processed = hr.getResult();
            }
          } catch (Exception e) {
            logger.warn("{} threw unexpected exception", handler, e);
          }
        }
      }
    }
    return new HandlerResult<>(processed, abort);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::connection=%s, operationExceptionHandler=%s, " +
        "operationResponseHandlers=%s]",
        getClass().getName(),
        hashCode(),
        connection,
        operationExceptionHandler,
        Arrays.toString(operationResponseHandlers));
  }


  /**
   * Exception handler that invokes {@link Connection#reopen(BindRequest)} when
   * an operation exception occurs and then invokes the operation again.
   */
  public class ReopenOperationExceptionHandler
    extends AbstractRetryOperationExceptionHandler<Q, S>
  {

    /** Bind request to use when reopening a connection. */
    private final BindRequest bindRequest;


    /** Default constructor. */
    public ReopenOperationExceptionHandler()
    {
      bindRequest = null;
    }


    /**
     * Creates a new reopen operation exception handler.
     *
     * @param  request  to bind with on reopen
     */
    public ReopenOperationExceptionHandler(final BindRequest request)
    {
      bindRequest = request;
    }


    @Override
    protected void handleInternal(
      final Connection conn,
      final Q request,
      final Response<S> response)
      throws LdapException
    {
      logger.warn("Operation exception encountered, reopening connection");
      if (bindRequest != null) {
        conn.reopen(bindRequest);
      } else {
        conn.reopen();
      }
    }


    @Override
    protected HandlerResult<Response<S>> createResult(
      final Connection conn,
      final Q request,
      final Response<S> response)
      throws LdapException
    {
      return new HandlerResult<>(invoke(request));
    }


    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::retry=%s, retryWait=%s, retryBackoff=%s, bindRequest=%s]",
          getClass().getName(),
          hashCode(),
          getRetry(),
          getRetryWait(),
          getRetryBackoff(),
          bindRequest);
    }
  }
}
