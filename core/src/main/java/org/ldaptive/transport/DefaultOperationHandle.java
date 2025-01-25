/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.ldaptive.AbandonRequest;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.BindRequest;
import org.ldaptive.BindResponse;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Message;
import org.ldaptive.ModifyDnRequest;
import org.ldaptive.ModifyDnResponse;
import org.ldaptive.ModifyRequest;
import org.ldaptive.ModifyResponse;
import org.ldaptive.OperationHandle;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.CancelRequest;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.ExtendedRequest;
import org.ldaptive.extended.ExtendedResponse;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.StartTLSRequest;
import org.ldaptive.extended.UnsolicitedNotification;
import org.ldaptive.handler.AbandonOperationException;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle that notifies on the components of an LDAP operation request.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public class DefaultOperationHandle<Q extends Request, S extends Result> implements OperationHandle<Q, S>
{

  /** Predicate that requires any result message except unsolicited. */
  private static final Predicate<Message> DEFAULT_RESPONSE_TIMEOUT_CONDITION =
    new Predicate<>() {
      @Override
      public boolean test(final Message message)
      {
        return message instanceof Result && !(message instanceof UnsolicitedNotification);
      }

      @Override
      public String toString()
      {
        return "DEFAULT_RESPONSE_TIMEOUT_CONDITION";
      }
    };

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Protocol request to send. */
  private final Q request;

  /** Time to wait for a response. */
  private final Duration responseTimeout;

  /** Connection to send the request on. */
  private TransportConnection connection;

  /** Protocol message ID. */
  private Integer messageID;

  /** Functions to handle response results. */
  private ResultHandler[] onResult;

  /** Functions to handle response controls. */
  private ResponseControlHandler[] onControl;

  /** Functions to handle referral URLs. */
  private ReferralHandler[] onReferral;

  /** Functions to handle intermediate responses. */
  private IntermediateResponseHandler[] onIntermediate;

  /** Function to handle exceptions. */
  private ExceptionHandler onException;

  /** Function to handle unsolicited notifications. */
  private UnsolicitedNotificationHandler[] onUnsolicitedNotification;

  /** Function to chase referrals. */
  private ReferralResultHandler<S> onReferralResult;

  /** Function to run when the operation completes. */
  private CompleteHandler onComplete;

  /** Function to run when a result is received to determine whether an exception should be raised. */
  private ResultPredicate throwCondition;

  /** Semaphore to determine when a response has been received. */
  private final Semaphore responseSemaphore = new Semaphore(0);

  /** Timestamp when the handle was created. */
  private final Instant creationTime = Instant.now();

  /** Timestamp when the request was sent. See {@link TransportConnection#write(DefaultOperationHandle)}. */
  private Instant sentTime;

  /** Timestamp when the result was received or an exception occurred. */
  private Instant receivedTime;

  /** Timestamp when the request was abandoned. */
  private Instant abandonedTime;

  /** Whether this handle has consumed any messages. */
  private boolean consumedMessage;

  /** Protocol response result. */
  private S result;

  /** Exception encountered attempting to process the request. */
  private LdapException exception;


  /**
   * Creates a new operation handle.
   *
   * @param  req  request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  public DefaultOperationHandle(final Q req, final TransportConnection conn, final Duration timeout)
  {
    if (req == null) {
      throw new IllegalArgumentException("Request cannot be null");
    }
    if (conn == null) {
      throw new IllegalArgumentException("Connection cannot be null");
    }
    request = req;
    connection = conn;
    responseTimeout = timeout;
  }


  /**
   * Returns a predicate to determine whether the responseTimeout semaphore should be released.
   *
   * @return  response timeout condition
   */
  protected Predicate<Message> getResponseTimeoutCondition()
  {
    return DEFAULT_RESPONSE_TIMEOUT_CONDITION;
  }


  @Override
  public DefaultOperationHandle<Q, S> send()
  {
    if (sentTime != null) {
      throw new IllegalStateException("Request for handle " + this + " has already been sent");
    }
    if (connection == null) {
      throw new IllegalStateException("Cannot execute request for handle " + this + ", connection is null");
    }
    connection.write(this);
    return this;
  }


  @Override
  public S await()
    throws LdapException
  {
    try {
      if (Duration.ZERO.equals(responseTimeout)) {
        do {
          logger.trace("await waiting to acquire {} for handle {}", responseSemaphore, this);
          responseSemaphore.acquire();
          logger.trace("await acquired {} for handle {}", responseSemaphore, this);
        } while (result == null && exception == null);
      } else {
        do {
          logger.trace("await waiting to acquire {} for handle {}", responseSemaphore, this);
          if (!responseSemaphore.tryAcquire(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
            abandon(
              new LdapException(
                ResultCode.LDAP_TIMEOUT,
                "No response received in " + responseTimeout.toMillis() + "ms for handle " + this));
            logger.trace("await failed to acquire {} and abandoned handle {}", responseSemaphore, this);
            break;
          }
          logger.trace("await acquired {} for handle {}", responseSemaphore, this);
        } while (result == null && exception == null);
      }
    } catch (InterruptedException e) {
      logger.trace("await interrupted acquiring {} for handle {} waiting for response", responseSemaphore, this, e);
      abandon(new LdapException(ResultCode.LOCAL_ERROR, e));
    }
    if (result != null && exception == null) {
      logger.trace("await received result {} for handle {}", result, this);
      if (ResultCode.REFERRAL == result.getResultCode() && onReferralResult != null) {
        final S referralResult = processReferralResult(result, onReferralResult);
        evaluateThrowCondition(referralResult);
        return referralResult;
      }
      evaluateThrowCondition(result);
      return result;
    }
    if (exception == null) {
      throw new LdapException(
        ResultCode.LOCAL_ERROR,
        "Response completed for handle " + this + " without a result or exception");
    }
    throw exception;
  }


  /**
   * Invokes {@link #throwCondition}, if it exists, for the supplied result.
   *
   * @param  r  to evaluate
   *
   * @throws  LdapException  if the condition fails
   */
  protected void evaluateThrowCondition(final Result r)
    throws LdapException
  {
    if (throwCondition != null) {
      throwCondition.testAndThrow(r);
    }
  }


  @Override
  public DefaultOperationHandle<Q, S> onResult(final ResultHandler... function)
  {
    onResult = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onControl(final ResponseControlHandler... function)
  {
    onControl = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onReferral(final ReferralHandler... function)
  {
    onReferral = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onIntermediate(final IntermediateResponseHandler... function)
  {
    onIntermediate = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onUnsolicitedNotification(final UnsolicitedNotificationHandler... function)
  {
    onUnsolicitedNotification = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onReferralResult(final ReferralResultHandler<S> function)
  {
    onReferralResult = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onException(final ExceptionHandler function)
  {
    onException = initializeMessageFunctional(function);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onComplete(final CompleteHandler function)
  {
    onComplete = function;
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> throwIf(final ResultPredicate function)
  {
    throwCondition = function;
    return this;
  }


  /**
   * Iterates over the supplied functions and creates a new, initialized {@link MessageFunctional} if any exists.
   *
   * @param  <T>  type of functions
   * @param  functions  to initialize
   *
   * @return  array of functions with any {@link MessageFunctional} initialized
   */
  @SuppressWarnings("unchecked")
  protected final <T> T[] initializeMessageFunctional(final T... functions)
  {
    if (functions != null) {
      final boolean hasMessageFunctional = Arrays.stream(functions).anyMatch(f -> f instanceof MessageFunctional);
      if (hasMessageFunctional) {
        final T[] copy = LdapUtils.copyArray(functions);
        for (int i = 0; i < copy.length; i++) {
          copy[i] = initializeMessageFunctional(copy[i]);
        }
        return copy;
      }
    }
    return functions;
  }


  /**
   * Creates a new {@link MessageFunctional} if function is of that type and initializes it.
   *
   * @param  <T>  type of function
   * @param  function  to initialize
   *
   * @return  initialized message functional or the same function
   */
  @SuppressWarnings("unchecked")
  protected final <T> T initializeMessageFunctional(final T function)
  {
    if (function instanceof MessageFunctional) {
      final MessageFunctional mf = ((MessageFunctional) function).newInstance();
      mf.setConnection(connection);
      mf.setRequest(request);
      mf.setHandle(this);
      if (((MessageFunctional) function).isFrozen()) {
        mf.freeze();
      }
      return (T) mf;
    }
    return function;
  }


  @Override
  public void abandon()
  {
    abandon(new LdapException(ResultCode.USER_CANCELLED, "Request abandoned"));
  }


  /**
   * Abandons this operation. Any threads waiting on the result will receive an empty result. See {@link
   * TransportConnection#operation(AbandonRequest)}.
   *
   * @param  cause  the reason this request was abandoned
   */
  public void abandon(final LdapException cause)
  {
    if (abandonedTime != null) {
      logger.warn("Request has already been abandoned for {}.", this);
      return;
    }
    if (sentTime == null) {
      logger.warn("Request has not been sent for {}.", this);
    }
    // Bind, unbind, StartTLS and Cancel cannot be abandoned
    if (!(request instanceof BindRequest ||
          request instanceof UnbindRequest ||
          request instanceof StartTLSRequest ||
          request instanceof CancelRequest)) {
      // Don't abandon a request if the response has been received
      if (receivedTime == null) {
        try {
          connection.operation(new AbandonRequest(messageID));
        } catch (Exception e) {
          logger.debug("Could not abandon operation for {}", this, e);
        } finally {
          abandonedTime = Instant.now();
          exception(cause);
        }
      } else {
        exception(cause);
      }
    } else {
      exception(cause);
    }
  }


  @Override
  public ExtendedOperationHandle cancel()
  {
    if (sentTime == null) {
      throw new IllegalStateException(
        "Request has not been sent for handle " + this + ". Invoke send before calling this method.");
    }
    // Don't cancel a request if the response has been received
    if (receivedTime != null) {
      throw new IllegalStateException(
        "Operation completed for handle " + this + ". Cancel cannot be invoked.");
    }
    final CompleteHandler completeHandler = onComplete;
    onComplete = null;
    final ExtendedOperationHandle handle = connection.operation(new CancelRequest(messageID));
    if (completeHandler != null) {
      handle.onComplete(completeHandler);
    }
    return handle;
  }


  /**
   * Whether the supplied result belongs to this handle.
   *
   * @param  r  to inspect
   *
   * @return  whether the supplied result belong to this handle
   */
  private boolean supports(final Result r)
  {
    if (messageID != r.getMessageID()) {
      return false;
    }
    boolean supports = false;
    if (request instanceof AddRequest && r instanceof AddResponse) {
      supports = true;
    } else if (request instanceof BindRequest && r instanceof BindResponse) {
      supports = true;
    } else if (request instanceof CompareRequest && r instanceof CompareResponse) {
      supports = true;
    } else if (request instanceof DeleteRequest && r instanceof DeleteResponse) {
      supports = true;
    } else if (request instanceof ExtendedRequest && r instanceof ExtendedResponse) {
      supports = true;
    } else if (request instanceof ModifyDnRequest && r instanceof ModifyDnResponse) {
      supports = true;
    } else if (request instanceof ModifyRequest && r instanceof ModifyResponse) {
      supports = true;
    } else if (request instanceof SearchRequest && r instanceof SearchResponse) {
      supports = true;
    }
    return supports;
  }


  /**
   * Returns the message ID assigned to this handle.
   *
   * @return  message ID
   */
  public Integer getMessageID()
  {
    return messageID;
  }


  @Override
  public Instant getSentTime()
  {
    return sentTime;
  }


  @Override
  public Instant getReceivedTime()
  {
    return receivedTime;
  }


  @Override
  public Instant getAbandonedTime()
  {
    return abandonedTime;
  }


  public ResultHandler[] getOnResult()
  {
    return onResult;
  }


  public ResponseControlHandler[] getOnControl()
  {
    return onControl;
  }


  public ReferralHandler[] getOnReferral()
  {
    return onReferral;
  }


  public IntermediateResponseHandler[] getOnIntermediate()
  {
    return onIntermediate;
  }


  public ExceptionHandler getOnException()
  {
    return onException;
  }


  public CompleteHandler getOnComplete()
  {
    return onComplete;
  }


  public ResultPredicate getThrowCondition()
  {
    return throwCondition;
  }


  public UnsolicitedNotificationHandler[] getOnUnsolicitedNotification()
  {
    return onUnsolicitedNotification;
  }


  public ReferralResultHandler<S> getOnReferralResult()
  {
    return onReferralResult;
  }


  /**
   * Returns whether this handle has consumed any messages.
   *
   * @return  whether this handle has consumed any messages
   */
  public boolean hasConsumedMessage()
  {
    return consumedMessage;
  }


  /**
   * Returns the request.
   *
   * @return  request
   */
  public Q getRequest()
  {
    return request;
  }


  /**
   * Sets the message ID.
   *
   * @param  id  message ID
   */
  public void messageID(final int id)
  {
    if (messageID != null) {
      throw new IllegalStateException("Message ID already assigned.");
    }
    messageID = id;
  }


  /**
   * Sets the sent time to now.
   */
  public void sent()
  {
    if (sentTime != null) {
      throw new IllegalStateException("Message already sent.");
    }
    sentTime = Instant.now();
  }


  /**
   * Invokes {@link #onResult} and sets the result. Handle is considered done when this is invoked.
   *
   * @param  r  result
   */
  public void result(final S r)
  {
    processResult(r);
    finalizeResult(r);
  }


  /**
   * Perform checks on the supplied result and invoke any configured {@link #onResult} handlers.
   *
   * @param  r  to process
   */
  protected void processResult(final S r)
  {
    if (result != null) {
      throw new IllegalStateException("Result already received.");
    }
    if (r == null) {
      final IllegalArgumentException e = new IllegalArgumentException("Result cannot be null for handle " + this);
      notifyExceptionHandlers(new LdapException(e));
      throw e;
    }
    if (!supports(r)) {
      final IllegalArgumentException e = new IllegalArgumentException("Invalid result " + r + " for handle " + this);
      notifyExceptionHandlers(new LdapException(e));
      throw e;
    }
    if (onResult != null) {
      for (ResultHandler func : onResult) {
        try {
          func.accept(r);
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
  }


  /**
   * Sets {@link #result}, notifies that message has been consumed and invokes {@link #complete()}.
   *
   * @param  r  to finalize
   */
  protected void finalizeResult(final S r)
  {
    result = r;
    consumedMessage(r);
    complete();
  }


  /**
   * Invokes {@link #onControl}.
   *
   * @param  c  response control
   */
  public void control(final ResponseControl c)
  {
    if (onControl != null) {
      for (ResponseControlHandler func : onControl) {
        try {
          func.accept(c);
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
  }


  /**
   * Invokes {@link #onReferral}.
   *
   * @param  url  referral url
   */
  public void referral(final String... url)
  {
    if (onReferral != null) {
      for (ReferralHandler func : onReferral) {
        try {
          func.accept(url);
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
  }


  /**
   * Invokes {@link #onIntermediate}.
   *
   * @param  r  intermediate response
   */
  public void intermediate(final IntermediateResponse r)
  {
    if (getMessageID() != r.getMessageID()) {
      final IllegalArgumentException e = new IllegalArgumentException(
        "Invalid intermediate response " + r + " for handle " + this);
      notifyExceptionHandlers(new LdapException(e));
      throw e;
    }
    if (onIntermediate != null) {
      for (IntermediateResponseHandler func : onIntermediate) {
        try {
          func.accept(r);
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
    consumedMessage(r);
  }


  /**
   * Invokes {@link #onUnsolicitedNotification}.
   *
   * @param  u  unsolicited notification
   */
  public void unsolicitedNotification(final UnsolicitedNotification u)
  {
    if (onUnsolicitedNotification != null) {
      for (UnsolicitedNotificationHandler func : onUnsolicitedNotification) {
        try {
          func.accept(u);
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
    consumedMessage(u);
  }


  /**
   * Invokes {@link #notifyExceptionHandlers(LdapException)} by wrapping the supplied exception in
   * {@link LdapException}. If the supplied exception is an instance of {@link AbandonOperationException},
   * {@link #abandon(LdapException)} is invoked.
   *
   * @param  e  to process
   */
  protected void processHandlerException(final Exception e)
  {
    if (e.getCause() instanceof LdapException) {
      final LdapException ldapEx = (LdapException) e.getCause();
      if (ldapEx instanceof AbandonOperationException) {
        abandon(ldapEx);
      } else {
        notifyExceptionHandlers(ldapEx);
      }
    } else {
      notifyExceptionHandlers(new LdapException(e));
    }
  }


  /**
   * Invokes any configured exception handlers with the supplied exception.
   *
   * @param  e  exception
   */
  protected void notifyExceptionHandlers(final LdapException e)
  {
    if (exception != null) {
      throw new IllegalStateException("Exception already received.");
    }
    if (e == null) {
      throw new IllegalArgumentException("Exception cannot be null for handle " + this);
    }
    if (onException != null) {
      try {
        onException.accept(e);
      } catch (Exception ex) {
        logger.warn("Exception consumer {} in handle {} threw an exception", onException, this, ex);
      }
    }
  }


  /**
   * Invokes {@link #notifyExceptionHandlers(LdapException)} followed by {@link #complete()}.
   *
   * @param  e  exception
   */
  public void exception(final LdapException e)
  {
    notifyExceptionHandlers(e);
    exception = e;
    consumedMessage();
    complete();
  }


  /**
   * Indicates that a protocol message was consumed by a supplied consumer.
   */
  protected void consumedMessage()
  {
    consumedMessage(true);
  }


  /**
   * Indicates that a protocol message was consumed by a supplied consumer.
   *
   * @param  message  that was consumed
   */
  protected void consumedMessage(final Message message)
  {
    consumedMessage(getResponseTimeoutCondition().test(message));
  }


  /**
   * Indicates that a protocol message was consumed by a supplied consumer.
   *
   * @param  signalResponseSemaphore  whether to signal the response semaphore
   */
  private void consumedMessage(final boolean signalResponseSemaphore)
  {
    consumedMessage = true;
    if (signalResponseSemaphore) {
      responseSemaphore.release();
    }
  }


  /**
   * Releases the latch and sets the response as received. Invokes {@link #onComplete}. Handle is considered done when
   * this is invoked.
   */
  private synchronized void complete()
  {
    try {
      if (receivedTime != null) {
        logger.debug("Operation already complete for handle {}", this);
        return;
      }
      receivedTime = Instant.now();
      if (onComplete != null) {
        try {
          onComplete.execute();
        } catch (Exception e) {
          logger.warn("Complete consumer {} in handle {} threw an exception", onComplete, this, e);
        }
      }
    } finally {
      try {
        if (connection != null) {
          connection.complete(this);
        }
      } catch (Exception e) {
        logger.warn("Connection {} complete threw an exception for handle {}", connection, this, e);
      } finally {
        connection = null;
      }
    }
  }


  /**
   * Invokes the supplied referral result handler and returns the result of that handler.
   *
   * @param  original  operation result
   * @param  handler  to invoke
   *
   * @return  result returned by the handler
   *
   * @throws  LdapException  if the handler throws an LdapException
   * @throws  IllegalArgumentException  if original is not a referral
   * @throws  IllegalStateException  if the handler does not return a valid result
   */
  protected S processReferralResult(final S original, final ReferralResultHandler<S> handler)
    throws LdapException
  {
    if (ResultCode.REFERRAL != original.getResultCode()) {
      throw new LdapException("Cannot process referral result for " + original);
    }
    final S handlerResponse;
    try {
      handlerResponse = handler.apply(original);
    } catch (Exception ex) {
      if (ex.getCause() instanceof LdapException) {
        throw (LdapException) ex.getCause();
      }
      throw new LdapException("Referral result handler " + handler + " threw exception", ex);
    }
    if (handlerResponse == null) {
      throw new LdapException("Referral result handler " + handler + " returned null result");
    }
    return handlerResponse;
  }


  @Override
  public String toString()
  {
    return getClass().getName() +
      "@" + hashCode() + "::" +
      "messageID=" + messageID + ", " +
      "request=" + request + ", " +
      "connection=" + connection + ", " +
      "responseTimeout=" + responseTimeout + ", " +
      "creationTime=" + creationTime + ", " +
      "sentTime=" + sentTime + ", " +
      "receivedTime=" + receivedTime + ", " +
      "consumedMessage=" + consumedMessage + ", " +
      "result=" + result + ", " +
      "exception=" + exception;
  }
}
