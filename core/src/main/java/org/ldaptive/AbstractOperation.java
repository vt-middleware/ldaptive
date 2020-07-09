/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.RequestHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Base class for operations.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public abstract class AbstractOperation<Q extends Request, S extends Result> implements Operation<Q, S>
{

  /** Connection factory. */
  private ConnectionFactory connectionFactory;

  /** Functions to handle requests. */
  private RequestHandler<Q>[] requestHandlers;

  /** Functions to handle response results. */
  private ResultHandler[] resultHandlers;

  /** Functions to handle response controls. */
  private ResponseControlHandler[] controlHandlers;

  /** Functions to handle referrals. */
  private ReferralHandler[] referralHandlers;

  /** Functions to handle intermediate responses. */
  private IntermediateResponseHandler[] intermediateResponseHandlers;

  /** Function to handle exceptions. */
  private ExceptionHandler exceptionHandler;

  /** Function to test results. */
  private ResultPredicate throwCondition;

  /** Functions to handle unsolicited notifications. */
  private UnsolicitedNotificationHandler[] unsolicitedNotificationHandlers;


  /**
   * Default constructor.
   */
  public AbstractOperation() {}


  /**
   * Creates a new abstract operation.
   *
   * @param  factory  connection factory
   */
  public AbstractOperation(final ConnectionFactory factory)
  {
    setConnectionFactory(factory);
  }


  public ConnectionFactory getConnectionFactory()
  {
    return connectionFactory;
  }


  public void setConnectionFactory(final ConnectionFactory factory)
  {
    connectionFactory = factory;
  }


  public RequestHandler<Q>[] getRequestHandlers()
  {
    return requestHandlers;
  }


  public void setRequestHandlers(final RequestHandler<Q>... handlers)
  {
    requestHandlers = handlers;
  }


  public ResultHandler[] getResultHandlers()
  {
    return resultHandlers;
  }


  public void setResultHandlers(final ResultHandler... handlers)
  {
    resultHandlers = handlers;
  }


  public ResponseControlHandler[] getControlHandlers()
  {
    return controlHandlers;
  }


  public void setControlHandlers(final ResponseControlHandler... handlers)
  {
    controlHandlers = handlers;
  }


  public ReferralHandler[] getReferralHandlers()
  {
    return referralHandlers;
  }


  public void setReferralHandlers(final ReferralHandler... handlers)
  {
    referralHandlers = handlers;
  }


  public IntermediateResponseHandler[] getIntermediateResponseHandlers()
  {
    return intermediateResponseHandlers;
  }


  public void setIntermediateResponseHandlers(final IntermediateResponseHandler... handlers)
  {
    intermediateResponseHandlers = handlers;
  }


  public ExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }


  public void setExceptionHandler(final ExceptionHandler handler)
  {
    exceptionHandler = handler;
  }


  public ResultPredicate getThrowCondition()
  {
    return throwCondition;
  }


  public void setThrowCondition(final ResultPredicate function)
  {
    throwCondition = function;
  }


  public UnsolicitedNotificationHandler[] getUnsolicitedNotificationHandlers()
  {
    return unsolicitedNotificationHandlers;
  }


  public void setUnsolicitedNotificationHandlers(final UnsolicitedNotificationHandler... handlers)
  {
    unsolicitedNotificationHandlers = handlers;
  }


  /**
   * Applies any configured request handlers to the supplied request. Returns the supplied request unaltered if no
   * request handlers are configured.
   *
   * @param  request  to configure
   *
   * @return  configured request
   */
  protected Q configureRequest(final Q request)
  {
    if (requestHandlers == null || requestHandlers.length == 0) {
      return request;
    }
    Q newRequest = request;
    for (RequestHandler<Q> h : requestHandlers) {
      newRequest = h.apply(newRequest);
    }
    return newRequest;
  }


  /**
   * Adds configured functions to the supplied handle.
   *
   * @param  handle  to configure
   *
   * @return  configured handle
   */
  protected OperationHandle<Q, S> configureHandle(final OperationHandle<Q, S> handle)
  {
    return handle
      .onControl(getControlHandlers())
      .onReferral(getReferralHandlers())
      .onIntermediate(getIntermediateResponseHandlers())
      .onException(getExceptionHandler())
      .throwIf(getThrowCondition())
      .onUnsolicitedNotification(getUnsolicitedNotificationHandlers())
      .onResult(getResultHandlers());
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("connectionFactory=").append(connectionFactory).append(", ")
      .append("requestHandlers=").append(Arrays.toString(requestHandlers)).append(", ")
      .append("resultHandlers=").append(Arrays.toString(resultHandlers)).append(", ")
      .append("controlHandlers=").append(Arrays.toString(controlHandlers)).append(", ")
      .append("referralHandlers=").append(Arrays.toString(referralHandlers)).append(", ")
      .append("intermediateResponseHandlers=").append(Arrays.toString(intermediateResponseHandlers)).append(", ")
      .append("exceptionHandler=").append(exceptionHandler).append(", ")
      .append("throwCondition=").append(throwCondition).append(", ")
      .append("unsolicitedNotificationHandlers=").append(Arrays.toString(unsolicitedNotificationHandlers)).toString();
  }


  /**
   * Base class for operation builders.
   *
   * @param  <B>  type of builder
   * @param  <T>  type of operation
   */
  protected abstract static class AbstractBuilder<B, T extends AbstractOperation>
  {

    /** Operation to build. */
    protected final T object;


    /**
     * Creates a new abstract builder.
     *
     * @param  t  operation to build
     */
    protected AbstractBuilder(final T t)
    {
      object = t;
    }


    /**
     * Returns this builder.
     *
     * @return  builder
     */
    protected abstract B self();


    /**
     * Sets the connection factory.
     *
     * @param  factory  to set
     *
     * @return  this builder
     */
    public B factory(final ConnectionFactory factory)
    {
      object.setConnectionFactory(factory);
      return self();
    }


    /**
     * Sets the functions to execute before a request is sent.
     *
     * @param  handlers  to execute on a request
     *
     * @return  this builder
     */
    public B onRequest(final RequestHandler... handlers)
    {
      object.setRequestHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when a result is received.
     *
     * @param  handlers  to execute on a result
     *
     * @return  this builder
     */
    public B onResult(final ResultHandler... handlers)
    {
      object.setResultHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when a control is received.
     *
     * @param  handlers  to execute on a control
     *
     * @return  this builder
     */
    public B onControl(final ResponseControlHandler... handlers)
    {
      object.setControlHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when a referral is received.
     *
     * @param  handlers  to execute on a referral
     *
     * @return  this builder
     */
    public B onReferral(final ReferralHandler... handlers)
    {
      object.setReferralHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when an intermediate response is received.
     *
     * @param  handlers  to execute on an intermediate response
     *
     * @return  this builder
     */
    public B onIntermediate(final IntermediateResponseHandler... handlers)
    {
      object.setIntermediateResponseHandlers(handlers);
      return self();
    }


    /**
     * Sets the functions to execute when an unsolicited notification is received.
     *
     * @param  handlers  to execute on an unsolicited notification
     *
     * @return  this builder
     */
    public B onUnsolicitedNotification(final UnsolicitedNotificationHandler... handlers)
    {
      object.setUnsolicitedNotificationHandlers(handlers);
      return self();
    }


    /**
     * Sets the function to execute when an exception occurs.
     *
     * @param  handler  to execute on an exception occurs
     *
     * @return  this builder
     */
    public B onException(final ExceptionHandler handler)
    {
      object.setExceptionHandler(handler);
      return self();
    }


    /**
     * Sets the function to test a result.
     *
     * @param  function  to test a result
     *
     * @return  this builder
     */
    public B throwIf(final ResultPredicate function)
    {
      object.setThrowCondition(function);
      return self();
    }


    /**
     * Returns the message.
     *
     * @return  message
     */
    public T build()
    {
      return object;
    }
  }
}
