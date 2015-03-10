/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.Response;
import org.ldaptive.async.AsyncRequest;

/**
 * Response listener.
 *
 * @author  Middleware Services
 */
public interface ResponseListener
{


  /**
   * Invoked when an asynchronous operation has begun.
   *
   * @param  request  to abandon this operation
   */
  void asyncRequestReceived(AsyncRequest request);


  /**
   * Invoked when a response is received from a provider indicating the operation has completed.
   *
   * @param  response  containing the result
   */
  void responseReceived(Response<Void> response);


  /**
   * Invoked when an exception is thrown from a provider indicating the operation cannot be completed.
   *
   * @param  exception  thrown from the async operation
   */
  void exceptionReceived(Exception exception);
}
