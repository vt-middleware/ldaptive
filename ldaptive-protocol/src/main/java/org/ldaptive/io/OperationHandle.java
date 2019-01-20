/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.io;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.ldaptive.control.ResponseControl;
import org.ldaptive.protocol.AbandonRequest;
import org.ldaptive.protocol.IntermediateResponse;
import org.ldaptive.protocol.Request;
import org.ldaptive.protocol.Result;
import org.ldaptive.protocol.UnsolicitedNotification;

/**
 * Handle that notifies on the components of an LDAP operation request.
 *
 * @author  Middleware Services
 */
public class OperationHandle
{

  /** Protocol request to send. */
  private Request request;

  /** Connection to send the request on. */
  private Connection connection;

  /** Time to wait for a response. */
  private Duration responseTimeout;

  /** Protocol message ID. */
  private int messageID;

  /** Function to handle response results. */
  private Consumer<Result> onResult;

  /** Function to handle response controls. */
  private Consumer<ResponseControl> onControl;

  /** Function to handle intermediate responses. */
  private Consumer<IntermediateResponse> onIntermediate;

  /** Function to handle exceptions. */
  private Consumer<LdapException> onException;

  /** Function to handle unsolicited notifications. */
  private Consumer<UnsolicitedNotification> onUnsolicitedNotification;

  /** Latch to determine when a response has been received. */
  private CountDownLatch responseDone = new CountDownLatch(1);

  /** Timestamp when the request was sent. See {@link Connection#write(OperationHandle)}. */
  private Instant sentTime;

  /** Timestamp when the result was received or an exception occurred. */
  private Instant receivedTime;

  /** Whether this handle has consumed any messages. */
  private boolean consumedMessage;

  /** Protocol response result. */
  private Result result;

  /** Exception encountered attempting to process the request. */
  private LdapException exception;


  /**
   * Creates a new operation handle.
   *
   * @param  req  request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  OperationHandle(final Request req, final Connection conn, final Duration timeout)
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


  /**
   * Sends this request to the server. See {@link Connection#write(OperationHandle)}.
   *
   * @return  this handle
   *
   * @throws  IllegalStateException  if this request has already been sent
   */
  public OperationHandle send()
  {
    if (sentTime != null) {
      throw new IllegalStateException("Request has already been sent");
    }
    if (connection == null) {
      throw new IllegalStateException("Cannot execute request, connection is null");
    }
    try {
      connection.write(this);
    } finally {
      sentTime = Instant.now();
    }
    return this;
  }


  /**
   * Waits for a result or reports a timeout exception.
   *
   * @return  result of the operation or empty if the operation is abandoned
   *
   * @throws  IllegalStateException  if {@link #send()} has not been invoked
   * @throws  LdapException  if an error occurs executing the request
   */
  public Result await()
    throws LdapException
  {
    if (sentTime == null) {
      throw new IllegalStateException("Request has not been sent. Invoke execute before calling this method.");
    }
    try {
      if (!responseDone.await(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
        abandon(new TimeoutException("No response received in " + responseTimeout.toMillis() + "ms"));
      } else if (result != null && exception == null) {
        return result;
      }
    } catch (InterruptedException e) {
      exception(e);
    }
    if (exception == null) {
      throw new IllegalStateException("Response completed without a result or exception");
    }
    throw exception;
  }


  /**
   * Convenience method that invokes {@link #send()} followed by {@link #await()}. Provides a single method to make a
   * synchronous request.
   *
   * @return  result of the operation or empty if the operation is abandoned
   *
   * @throws  LdapException  if an error occurs executing the request
   */
  public Result execute()
    throws LdapException
  {
    return send().await();
  }


  /**
   * Sets the function to execute when a result is received.
   *
   * @param  function  to execute on a result
   *
   * @return  this handle
   */
  public OperationHandle onResult(final Consumer<Result> function)
  {
    onResult = function;
    return this;
  }


  /**
   * Sets the function to execute when a control is received.
   *
   * @param  function  to execute on a control
   *
   * @return  this handle
   */
  public OperationHandle onControl(final Consumer<ResponseControl> function)
  {
    onControl = function;
    return this;
  }


  /**
   * Sets the function to execute when an intermediate response is received.
   *
   * @param  function  to execute on an intermediate response
   *
   * @return  this handle
   */
  public OperationHandle onIntermediate(final Consumer<IntermediateResponse> function)
  {
    onIntermediate = function;
    return this;
  }


  /**
   * Sets the function to execute when an unsolicited notification is received.
   *
   * @param  function  to execute on an unsolicited notification
   *
   * @return  this handle
   */
  public OperationHandle onUnsolicitedNotification(final Consumer<UnsolicitedNotification> function)
  {
    onUnsolicitedNotification = function;
    return this;
  }


  /**
   * Sets the function to execute when an exception occurs.
   *
   * @param  function  to execute on an exception occurs
   *
   * @return  this handle
   */
  public OperationHandle onException(final Consumer<LdapException> function)
  {
    onException = function;
    return this;
  }


  /**
   * Abandons this operation. See {@link #abandon(Throwable)}.
   *
   * @throws  IllegalStateException  if the request has not been sent to the server
   */
  public void abandon()
  {
    abandon(new LdapException("Request abandoned"));
  }


  /**
   * Abandons this operation. Any threads waiting on the result will receive an empty result. See {@link
   * Connection#operation(AbandonRequest)}.
   *
   * @param  cause  the reason this request was abandoned
   *
   * @throws  IllegalStateException  if the request has not been sent to the server
   */
  public void abandon(final Throwable cause)
  {
    if (sentTime == null) {
      throw new IllegalStateException("Request has not been sent. Invoke execute before calling this method.");
    }
    if (receivedTime == null) {
      try {
        connection.operation(new AbandonRequest(messageID));
      } finally {
        exception(cause);
      }
    }
  }


  /**
   * Returns the time this operation sent a request.
   *
   * @return  sent time
   */
  public Instant getSentTime()
  {
    return sentTime;
  }


  /**
   * Returns the time this operation received a result or encountered an exception.
   *
   * @return  received time
   */
  public Instant getReceivedTime()
  {
    return receivedTime;
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
  Request getRequest()
  {
    return request;
  }


  /**
   * Sets the message ID.
   *
   * @param  id  message ID
   */
  void messageID(final int id)
  {
    messageID = id;
  }


  /**
   * Invokes {@link #onResult} and sets the result. Handle is considered done when this is invoked.
   *
   * @param  r  result
   */
  void result(final Result r)
  {
    if (r == null) {
      throw new IllegalArgumentException("Result cannot be null");
    }
    if (onResult != null) {
      onResult.accept(r);
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
  void control(final ResponseControl c)
  {
    if (onControl != null) {
      onControl.accept(c);
    }
  }


  /**
   * Invokes {@link #onIntermediate}.
   *
   * @param  r  intermediate response
   */
  void intermediate(final IntermediateResponse r)
  {
    if (onIntermediate != null) {
      onIntermediate.accept(r);
      consumedMessage();
    }
  }


  /**
   * Invokes {@link #onUnsolicitedNotification}.
   *
   * @param  u  unsolicited notification
   */
  void unsolicitedNotification(final UnsolicitedNotification u)
  {
    if (onUnsolicitedNotification != null) {
      onUnsolicitedNotification.accept(u);
      consumedMessage();
    }
  }


  /**
   * Invokes {@link #onException} and sets the result. Handle is considered done when this is invoked.
   *
   * @param  e  exception
   */
  void exception(final Throwable e)
  {
    if (e == null) {
      throw new IllegalArgumentException("Exception cannot be null");
    }
    final LdapException ldapEx;
    if (e instanceof LdapException) {
      ldapEx = (LdapException) e;
    } else {
      ldapEx = new LdapException(e);
    }
    if (onException != null) {
      onException.accept(ldapEx);
    }
    exception = ldapEx;
    complete();
  }


  /**
   * Indicates that a protocol message was consumed by a supplied consumer.
   */
  void consumedMessage()
  {
    consumedMessage = true;
  }


  /**
   * Releases the latch and sets the response as received.
   */
  private void complete()
  {
    try {
      responseDone.countDown();
    } finally {
      receivedTime = Instant.now();
      connection = null;
    }
  }
}
