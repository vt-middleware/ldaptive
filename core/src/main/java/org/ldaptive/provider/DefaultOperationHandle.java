/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.ldaptive.AbandonRequest;
import org.ldaptive.BindRequest;
import org.ldaptive.LdapException;
import org.ldaptive.OperationHandle;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.UnbindRequest;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.extended.CancelRequest;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.extended.IntermediateResponse;
import org.ldaptive.extended.StartTLSRequest;
import org.ldaptive.extended.UnsolicitedNotification;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
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

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Protocol request to send. */
  private Request request;

  /** Connection to send the request on. */
  private ProviderConnection connection;

  /** Time to wait for a response. */
  private Duration responseTimeout;

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

  /** Functions to handle exceptions. */
  private ExceptionHandler onException;

  /** Functions to handle unsolicited notifications. */
  private UnsolicitedNotificationHandler[] onUnsolicitedNotification;

  /** Functions to run when the operation completes. */
  private CompleteHandler onComplete;

  /** Latch to determine when a response has been received. */
  private final CountDownLatch responseDone = new CountDownLatch(1);

  /** Timestamp when the handle was created. */
  private final Instant creationTime = Instant.now();

  /** Timestamp when the request was sent. See {@link ProviderConnection#write(DefaultOperationHandle)}. */
  private Instant sentTime;

  /** Timestamp when the result was received or an exception occurred. */
  private Instant receivedTime;

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
  public DefaultOperationHandle(final Q req, final ProviderConnection conn, final Duration timeout)
  {
    if (req == null) {
      throw new IllegalArgumentException("Request cannot be null");
    }
    if (conn == null) {
      throw new IllegalArgumentException("Connection cannot be null");
    }
    if (timeout == null) {
      throw new IllegalArgumentException("Timeout cannot be null");
    }
    request = req;
    connection = conn;
    responseTimeout = timeout;
  }


  @Override
  public DefaultOperationHandle<Q, S> send()
  {
    if (sentTime != null) {
      throw new IllegalStateException("Request for handle " + this + " has already been sent");
    }
    if (connection == null) {
      throw new IllegalStateException("Cannot execute request for handle " + this + " , connection is null");
    }
    connection.write(this);
    return this;
  }


  @Override
  public S await()
    throws LdapException
  {
    try {
      if (responseTimeout == null) {
        responseDone.await();
        if (result != null && exception == null) {
          logger.trace("await received result {} for handle {}", result, this);
          return result;
        }
      } else {
        if (!responseDone.await(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
          abandon(new TimeoutException("No response received in " + responseTimeout.toMillis() + "ms"));
          logger.trace("await abandoned handle {}", this);
        } else if (result != null && exception == null) {
          logger.trace("await received result {} for handle {}", result, this);
          return result;
        }
      }
    } catch (InterruptedException e) {
      logger.trace("await interrupted for handle {} waiting for response", this, e);
      exception(e);
    }
    if (exception == null) {
      throw new IllegalStateException("Response completed for handle " + this + " without a result or exception");
    }
    throw exception;
  }


  @Override
  public DefaultOperationHandle<Q, S> onResult(final ResultHandler... function)
  {
    onResult = function;
    initializeMessageFunctional((Object[]) onResult);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onControl(final ResponseControlHandler... function)
  {
    onControl = function;
    initializeMessageFunctional((Object[]) onControl);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onReferral(final ReferralHandler... function)
  {
    onReferral = function;
    initializeMessageFunctional((Object[]) onReferral);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onIntermediate(final IntermediateResponseHandler... function)
  {
    onIntermediate = function;
    initializeMessageFunctional((Object[]) onIntermediate);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onUnsolicitedNotification(final UnsolicitedNotificationHandler... function)
  {
    onUnsolicitedNotification = function;
    initializeMessageFunctional((Object[]) onUnsolicitedNotification);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onException(final ExceptionHandler function)
  {
    onException = function;
    initializeMessageFunctional(onException);
    return this;
  }


  @Override
  public DefaultOperationHandle<Q, S> onComplete(final CompleteHandler function)
  {
    onComplete = function;
    return this;
  }


  /**
   * Iterates over the supplied functions, set the connection and request if the type is {@link MessageFunctional}.
   *
   * @param  functions  to initialize
   */
  @SuppressWarnings("unchecked")
  protected void initializeMessageFunctional(final Object... functions)
  {
    if (functions != null) {
      for (Object o : functions) {
        if (o instanceof MessageFunctional) {
          ((MessageFunctional) o).setConnection(connection);
          ((MessageFunctional) o).setRequest(request);
          ((MessageFunctional) o).setHandle(this);
        }
      }
    }
  }


  @Override
  public void abandon()
  {
    abandon(new LdapException("Request abandoned"));
  }


  /**
   * Abandons this operation. Any threads waiting on the result will receive an empty result. See {@link
   * ProviderConnection#operation(AbandonRequest)}.
   *
   * @param  cause  the reason this request was abandoned
   *
   * @throws  IllegalStateException  if the request has not been sent to the server
   */
  public void abandon(final Throwable cause)
  {
    if (sentTime == null) {
      throw new IllegalStateException(
        "Request has not been sent for handle " + this + ". Invoke execute before calling this method.");
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
        } finally {
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
        "Request has not been sent for handle " + this + ". Invoke execute before calling this method.");
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
   * Returns the message ID assigned to this handle.
   *
   * @return  message ID
   */
  public int getMessageID()
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


  public UnsolicitedNotificationHandler[] getOnUnsolicitedNotification()
  {
    return onUnsolicitedNotification;
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
  public Request getRequest()
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
    messageID = id;
  }


  /**
   * Sets the sent time to now.
   */
  public void sent()
  {
    sentTime = Instant.now();
  }


  /**
   * Invokes {@link #onResult} and sets the result. Handle is considered done when this is invoked.
   *
   * @param  r  result
   */
  public void result(final S r)
  {
    if (r == null) {
      throw new IllegalArgumentException("Result cannot be null for handle " + this);
    }
    if (onResult != null) {
      for (ResultHandler func : onResult) {
        try {
          func.accept(r);
        } catch (Exception ex) {
          logger.warn("Result function {} in handle {} threw an exception", func, this, ex);
        }
      }
      consumedMessage();
    }
    result = r;
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
          logger.warn("Control consumer {} in handle {} threw an exception", func, this, ex);
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
          logger.warn("Referral consumer {} in handle {} threw an exception", func, this, ex);
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
    if (onIntermediate != null) {
      for (Consumer<IntermediateResponse> func : onIntermediate) {
        try {
          func.accept(r);
        } catch (Exception ex) {
          logger.warn("Intermediate response consumer {} in handle {} threw an exception", func, this, ex);
        }
      }
      consumedMessage();
    }
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
          logger.warn("Unsolicited notification consumer {} in handle {} threw an exception", func, this, ex);
        }
      }
      consumedMessage();
    }
  }


  /**
   * Invokes {@link #onException} followed by {@link #complete()}.
   *
   * @param  e  exception
   */
  public void exception(final Throwable e)
  {
    if (e == null) {
      throw new IllegalArgumentException("Exception cannot be null for handle " + this);
    }
    final LdapException ldapEx;
    if (e instanceof LdapException) {
      ldapEx = (LdapException) e;
    } else {
      ldapEx = new LdapException(e);
    }
    if (onException != null) {
      try {
        onException.accept(ldapEx);
      } catch (Exception ex) {
        logger.warn("Exception consumer {} in handle {} threw an exception", onException, this, ex);
      }
    }
    exception = ldapEx;
    complete();
  }


  /**
   * Indicates that a protocol message was consumed by a supplied consumer.
   */
  protected void consumedMessage()
  {
    consumedMessage = true;
  }


  /**
   * Releases the latch and sets the response as received. Invokes {@link #onComplete}. Handle is considered done when
   * this is invoked.
   */
  private void complete()
  {
    if (receivedTime != null) {
      logger.warn("Operation already complete for handle {}", this);
      return;
    }
    try {
      responseDone.countDown();
    } finally {
      receivedTime = Instant.now();
      if (onComplete != null) {
        try {
          onComplete.execute();
        } catch (Exception ex) {
          logger.warn("Complete consumer {} in handle {} threw an exception", onComplete, this, ex);
        }
      }
      connection = null;
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("messageID=").append(messageID).append(", ")
      .append("request=").append(request).append(", ")
      .append("connection=").append(connection).append(", ")
      .append("responseTimeout=").append(responseTimeout).append(", ")
      .append("creationTime=").append(creationTime).append(", ")
      .append("sentTime=").append(sentTime).append(", ")
      .append("receivedTime=").append(receivedTime).append(", ")
      .append("consumedMessage=").append(consumedMessage).append(", ")
      .append("result=").append(result).append(", ")
      .append("exception=").append(exception).toString();
  }
}
