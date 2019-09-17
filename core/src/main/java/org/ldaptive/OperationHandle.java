/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Instant;
import org.ldaptive.extended.ExtendedOperationHandle;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of an LDAP operation request.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public interface OperationHandle<Q extends Request, S extends Result>
{


  /**
   * Sends this request to the server.
   *
   * @return  this handle
   *
   * @throws  IllegalStateException  if this request has already been sent
   */
  OperationHandle<Q, S> send();


  /**
   * Waits for a result or reports a timeout exception.
   *
   * @return  result of the operation or empty if the operation is abandoned
   *
   * @throws  LdapException  if an error occurs executing the request
   */
  S await() throws LdapException;


  /**
   * Convenience method that invokes {@link #send()} followed by {@link #await()}. Provides a single method to make a
   * synchronous request.
   *
   * @return  result of the operation or empty if the operation is abandoned
   *
   * @throws  LdapException  if an error occurs executing the request
   */
  default S execute()
    throws LdapException
  {
    return send().await();
  }


  /**
   * Sets the functions to execute when a result is received.
   *
   * @param  function  to execute on a result
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onResult(ResultHandler... function);


  /**
   * Sets the functions to execute when a control is received.
   *
   * @param  function  to execute on a control
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onControl(ResponseControlHandler... function);


  /**
   * Sets the functions to execute when a referral is received.
   *
   * @param  function  to execute on a referral
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onReferral(ReferralHandler... function);


  /**
   * Sets the functions to execute when an intermediate response is received.
   *
   * @param  function  to execute on an intermediate response
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onIntermediate(IntermediateResponseHandler... function);


  /**
   * Sets the functions to execute when an unsolicited notification is received.
   *
   * @param  function  to execute on an unsolicited notification
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onUnsolicitedNotification(UnsolicitedNotificationHandler... function);


  /**
   * Sets the function to execute when an exception occurs.
   *
   * @param  function  to execute when an exception occurs
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onException(ExceptionHandler function);


  /**
   * Sets the function to execute when the operation completes.
   *
   * @param  function  to execute on completion
   *
   * @return  this handle
   */
  OperationHandle<Q, S> onComplete(CompleteHandler function);


  /**
   * Abandons this operation.
   *
   * @throws  IllegalStateException  if the request has not been sent to the server
   */
  void abandon();


  /**
   * Cancels this operation. See {@link org.ldaptive.extended.CancelRequest}.
   *
   * @return  extended operation handle
   *
   * @throws  IllegalStateException  if the request has not been sent to the server
   */
  ExtendedOperationHandle cancel();


  /**
   * Returns the time this operation sent a request.
   *
   * @return  sent time
   */
  Instant getSentTime();


  /**
   * Returns the time this operation received a result or encountered an exception.
   *
   * @return  received time
   */
  Instant getReceivedTime();
}
