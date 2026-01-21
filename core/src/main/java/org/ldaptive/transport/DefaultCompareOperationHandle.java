/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

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
import org.ldaptive.handler.ReferralResultHandler;
import org.ldaptive.handler.ResponseControlHandler;
import org.ldaptive.handler.ResultHandler;
import org.ldaptive.handler.ResultPredicate;
import org.ldaptive.handler.UnsolicitedNotificationHandler;

/**
 * Handle that notifies on the components of a compare request.
 *
 * @author  Middleware Services
 */
public final class DefaultCompareOperationHandle
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
  public DefaultCompareOperationHandle(final CompareRequest req, final TransportConnection conn, final Duration timeout)
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
  public DefaultCompareOperationHandle onReferralResult(final ReferralResultHandler<CompareResponse> function)
  {
    super.onReferralResult(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onException(final ExceptionHandler function)
  {
    super.onException(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle throwIf(final ResultPredicate function)
  {
    super.throwIf(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onComplete(final CompleteHandler function)
  {
    super.onComplete(function);
    return this;
  }


  @Override
  public DefaultCompareOperationHandle onCompare(final CompareValueHandler... function)
  {
    onCompare = initializeMessageFunctional(function);
    return this;
  }


  /**
   * Return the compare value handler.
   *
   * @return  compare value handler
   */
  public CompareValueHandler[] getOnCompare()
  {
    return onCompare;
  }


  /**
   * Invokes {@link #onCompare}.
   *
   * @param  response  compare response
   */
  public void compare(final CompareResponse response)
  {
    if (getMessageID() != response.getMessageID()) {
      final IllegalArgumentException e = new IllegalArgumentException(
        "Invalid compare response " + response + " for handle " + this);
      notifyExceptionHandlers(new LdapException(e));
      throw e;
    }
    if (onCompare != null) {
      for (CompareValueHandler func : onCompare) {
        try {
          if (response.getResultCode() == ResultCode.COMPARE_TRUE) {
            func.accept(Boolean.TRUE);
          } else if (response.getResultCode() == ResultCode.COMPARE_FALSE) {
            func.accept(Boolean.FALSE);
          }
        } catch (Exception ex) {
          processHandlerException(ex);
        }
      }
    }
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " + "onCompare=" + Arrays.toString(onCompare);
  }
}
