/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import java.time.Duration;
import java.util.Arrays;
import org.ldaptive.CompareOperationHandle;
import org.ldaptive.CompareRequest;
import org.ldaptive.CompareResponse;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.handler.CompareValueHandler;
import org.ldaptive.handler.CompleteHandler;
import org.ldaptive.handler.ExceptionHandler;
import org.ldaptive.handler.IntermediateResponseHandler;
import org.ldaptive.handler.ReferralHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of a compare request.
 *
 * @author  Middleware Services
 */
public class DefaultCompareOperationHandle
  extends DefaultOperationHandle<CompareRequest, CompareResponse> implements CompareOperationHandle
{

  /** Functions to handle the compare result. */
  private CompareValueHandler[] onCompare;


  /**
   * Creates a new compare operation handle.
   *
   * @param  req  compare request to expect a response for
   * @param  conn  the request will be executed on
   * @param  timeout  duration to wait for a response
   */
  public DefaultCompareOperationHandle(final CompareRequest req, final ProviderConnection conn, final Duration timeout)
  {
    super(req, conn, timeout);
  }


  @Override
  public DefaultCompareOperationHandle send()
  {
    super.send();
    return this;
  }


  @Override
  public CompareResponse await()
    throws LdapException
  {
    return super.await();
  }


  @Override
  public DefaultCompareOperationHandle onResult(final ResultHandler... function)
  {
    super.onResult(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onControl(final ResponseControlHandler... function)
  {
    super.onControl(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onReferral(final ReferralHandler... function)
  {
    super.onReferral(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onIntermediate(final IntermediateResponseHandler... function)
  {
    super.onIntermediate(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onUnsolicitedNotification(final UnsolicitedNotificationHandler... function)
  {
    super.onUnsolicitedNotification(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onException(final ExceptionHandler function)
  {
    super.onException(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onComplete(final CompleteHandler function)
  {
    super.onComplete(function);
    return this;
  }


  /**
   * Sets the function to execute when a compare result is received.
   *
   * @param  function  to execute on a compare result
   *
   * @return  this handle
   */
  public DefaultCompareOperationHandle onCompare(final CompareValueHandler... function)
  {
    onCompare = function;
    initializeMessageFunctional((Object[]) onCompare);
    return this;
  }


  /**
   * Invokes {@link #onCompare}.
   *
   * @param  response  compare response
   */
  public void compare(final CompareResponse response)
  {
    if (onCompare != null) {
      for (CompareValueHandler func : onCompare) {
        try {
          if (response.getResultCode() == ResultCode.COMPARE_TRUE) {
            func.accept(Boolean.TRUE);
          } else if (response.getResultCode() == ResultCode.COMPARE_FALSE) {
            func.accept(Boolean.FALSE);
          }
        } catch (Exception ex) {
          logger.warn("Compare response consumer {} threw an exception", func, ex);
        }
      }
    }
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("onCompare=").append(Arrays.toString(onCompare)).toString();
  }
}
